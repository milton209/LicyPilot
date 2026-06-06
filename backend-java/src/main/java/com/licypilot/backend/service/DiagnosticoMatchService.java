package com.licypilot.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.licypilot.backend.dto.DiagnosticoBlocoDTO;
import com.licypilot.backend.model.*;
import com.licypilot.backend.util.LogPadrao;
import com.licypilot.backend.repository.AnaliseUsuarioRepository;
import com.licypilot.backend.repository.EmpresaRepository;
import com.licypilot.backend.repository.LicitacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class DiagnosticoMatchService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticoMatchService.class);
    private final AnaliseUsuarioRepository analiseUsuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final LicitacaoRepository licitacaoRepository;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public DiagnosticoMatchService(AnaliseUsuarioRepository analiseUsuarioRepository, 
                                 EmpresaRepository empresaRepository,
                                 LicitacaoRepository licitacaoRepository,
                                 ChatModel chatModel, 
                                 ObjectMapper objectMapper) {
        this.analiseUsuarioRepository = analiseUsuarioRepository;
        this.empresaRepository = empresaRepository;
        this.licitacaoRepository = licitacaoRepository;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    public AnaliseUsuario criarAnaliseUsuario(UUID licitacaoId, UUID empresaId) {
        Licitacao licitacao = licitacaoRepository.findById(licitacaoId)
                .orElseThrow(() -> new RuntimeException("Licitação não encontrada"));
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        AnaliseUsuario analise = new AnaliseUsuario();
        analise.setLicitacao(licitacao);
        analise.setEmpresa(empresa);
        analise.setStatusViabilidade(StatusViabilidade.REVISAO_MANUAL);
        return analiseUsuarioRepository.save(analise);
    }

    public AnaliseUsuario executarDiagnosticoCompleto(UUID analiseId) {
        AnaliseUsuario analise = analiseUsuarioRepository.findById(analiseId)
                .orElseThrow(() -> new RuntimeException("Análise não encontrada"));

        log.info("Iniciando Diagnóstico de Match para Análise ID: {}", analiseId);

        JsonNode masterJson = analise.getLicitacao().getMasterJson();
        if (masterJson == null || masterJson.isEmpty()) {
            throw new RuntimeException("Edital ainda não processado.");
        }

        Empresa empresa = analise.getEmpresa();
        ObjectNode diagnosticoFinal = objectMapper.createObjectNode();
        ObjectNode blocosTecnicos = objectMapper.createObjectNode();

        List<CompletableFuture<Void>> tarefas = new ArrayList<>();
        tarefas.add(agendarAnaliseBloco(blocosTecnicos, "habilitacao", "Habilitação", masterJson.path("habilitacao_detalhada"), empresa));
        tarefas.add(agendarAnaliseBloco(blocosTecnicos, "qualificacao_tecnica", "Qualificação Técnica", masterJson.path("qualificacao_tecnica_especifica"), empresa));
        tarefas.add(agendarAnaliseBloco(blocosTecnicos, "financeiro", "Financeiro", masterJson.path("prazos_valores_e_pagamento"), empresa));
        tarefas.add(agendarAnaliseBloco(blocosTecnicos, "regras_e_riscos", "Regras e Riscos", masterJson.path("regras_da_disputa"), empresa));

        CompletableFuture.allOf(tarefas.toArray(new CompletableFuture[0])).join();

        diagnosticoFinal.set("detalhes_por_area", blocosTecnicos);

        log.info("Gerando Veredito Final Humanizado...");
        String vereditoGeral = gerarVereditoGeral(blocosTecnicos, empresa);
        diagnosticoFinal.put("veredito_do_especialista", vereditoGeral);

        analise.setDiagnosticoJson(diagnosticoFinal);
        return analiseUsuarioRepository.save(analise);
    }

    public void executarDiagnosticoSse(UUID analiseId, org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter) {
        AnaliseUsuario analise = analiseUsuarioRepository.findById(analiseId)
                .orElseThrow(() -> new RuntimeException("Análise não encontrada"));

        // Marca como processando imediatamente
        analise.setStatusProcessamento(StatusProcessamento.PROCESSANDO);
        analiseUsuarioRepository.saveAndFlush(analise);

        JsonNode masterJson = analise.getLicitacao().getMasterJson();
        if (masterJson == null || masterJson.isEmpty()) {
            emitter.completeWithError(new RuntimeException("Edital ainda não processado."));
            return;
        }

        Empresa empresa = analise.getEmpresa();
        ObjectNode diagnosticoFinal = objectMapper.createObjectNode();
        ObjectNode blocosTecnicos = objectMapper.createObjectNode();

        CompletableFuture.runAsync(() -> {
            try {
                emitter.send("[STATUS:ANALISANDO_BLOCOS]\n\n");
                
                List<CompletableFuture<Void>> tarefas = new ArrayList<>();
                tarefas.add(agendarAnaliseBloco(blocosTecnicos, "habilitacao", "Habilitação", masterJson.path("habilitacao_detalhada"), empresa));
                tarefas.add(agendarAnaliseBloco(blocosTecnicos, "qualificacao_tecnica", "Qualificação Técnica", masterJson.path("qualificacao_tecnica_especifica"), empresa));
                tarefas.add(agendarAnaliseBloco(blocosTecnicos, "financeiro", "Financeiro", masterJson.path("prazos_valores_e_pagamento"), empresa));
                tarefas.add(agendarAnaliseBloco(blocosTecnicos, "regras_e_riscos", "Regras e Riscos", masterJson.path("regras_da_disputa"), empresa));

                CompletableFuture.allOf(tarefas.toArray(new CompletableFuture[0])).join();
                diagnosticoFinal.set("detalhes_por_area", blocosTecnicos);

                emitter.send("[STATUS:GERANDO_VEREDITO]\n\n");

                String prompt = String.format(
                    "Você é um consultor sênior em licitações. Com base nas análises técnicas abaixo, dê um veredito humanizado e direto para o dono da empresa %s.\n" +
                    "Diga se vale a pena participar, quais os riscos reais e dê conselhos práticos.\n" +
                    "Fale de forma amigável, como um consultor falando com um cliente.\n\n" +
                    "RESUMO DAS ÁREAS:\n%s\n\n" +
                    "ESCREVA UM PARÁGRAFO DE PARECER FINAL:",
                    empresa.getRazaoSocial(), blocosTecnicos.toPrettyString()
                );

                StringBuilder vereditoCompleto = new StringBuilder();
                
                // Consumindo o Flux do Spring AI e enviando para o SseEmitter
                chatModel.stream(prompt).toStream().forEach(token -> {
                    try {
                        if (token != null) {
                            vereditoCompleto.append(token);
                            // Enviamos o token exatamente como veio, mas garantimos que não haja cache
                            emitter.send(token);
                        }
                    } catch (Exception e) {
                        log.error("Erro ao enviar token SSE: {}", e.getMessage());
                    }
                });

                // SALVAMENTO FINAL NO BANCO DE DADOS (Persistência)
                try {
                    log.info("Persistindo diagnóstico final para análise ID: {}", analiseId);
                    diagnosticoFinal.put("veredito_do_especialista", vereditoCompleto.toString());
                    analise.setDiagnosticoJson(diagnosticoFinal);
                    analise.setStatusProcessamento(StatusProcessamento.CONCLUIDO);
                    analiseUsuarioRepository.saveAndFlush(analise); // Garante o commit imediato
                    emitter.complete();
                } catch (Exception e) {
                    log.error("Erro ao finalizar e salvar SSE: {}", e.getMessage());
                    emitter.completeWithError(e);
                }
            } catch (Exception e) {
                log.error("Erro processamento assíncrono SSE: {}", e.getMessage());
                // Em caso de erro, remove o status de processando
                analise.setStatusProcessamento(StatusProcessamento.ERRO);
                analiseUsuarioRepository.saveAndFlush(analise);
                emitter.completeWithError(e);
            }
        });
    }

    private CompletableFuture<Void> agendarAnaliseBloco(ObjectNode root, String chaveJson, String nomeAmigavel, JsonNode dadosEdital, Empresa empresa) {
        return CompletableFuture.runAsync(() -> {
            DiagnosticoBlocoDTO resultado = analisarBloco(nomeAmigavel, dadosEdital, empresa);
            synchronized (root) {
                root.set(chaveJson, objectMapper.valueToTree(resultado));
            }
        });
    }

    private DiagnosticoBlocoDTO analisarBloco(String nomeBloco, JsonNode dadosEdital, Empresa empresa) {
        if (dadosEdital.isMissingNode() || dadosEdital.isEmpty()) {
            return new DiagnosticoBlocoDTO("SIM", List.of(), "Área sem exigências críticas.", List.of(), "Tudo certo nesta área.");
        }

        BeanOutputConverter<DiagnosticoBlocoDTO> converter = new BeanOutputConverter<>(DiagnosticoBlocoDTO.class);
        
        String promptCompleto = String.format(
            "### REGRAS DE OURO (ESTRITAMENTE OBRIGATÓRIAS) ###\n" +
            "1. RESPONDA APENAS COM O OBJETO JSON PURO.\n" +
            "2. NÃO ADICIONE PREÂMBULOS, EXPLICAÇÕES OU TEXTO FORA DO JSON.\n" +
            "3. USE EXATAMENTE ESTAS CHAVES: \"atende\", \"pendencias\", \"justificativa\", \"trechos_originais\", \"veredito_especialista\".\n" +
            "4. NUNCA TRADUZA AS CHAVES PARA PORTUGUÊS OU USE LETRAS MAIÚSCULAS NELAS.\n\n" +
            "### TAREFA DE ANÁLISE ###\n" +
            "Analise tecnicamente o bloco de '%s' para a empresa %s.\n" +
            "Compare as exigências do Edital: %s\n" +
            "Considere os dados da Empresa:\n" +
            "- Capital R$ %.2f\n" +
            "- CNAEs/Tags: %s\n" +
            "- Acervo e Experiência Técnica: %s\n" +
            "IMPORTANTE: A empresa JÁ POSSUI estes documentos regularizados: %s. Se o edital pedir algum destes, considere como ATENDIDO.\n\n" +
            "### FORMATO DE RESPOSTA ESPERADO ###\n" +
            "%s",
            nomeBloco, empresa.getRazaoSocial(), dadosEdital.toString(), 
            empresa.getCapitalSocial() != null ? empresa.getCapitalSocial() : 0.0, 
            empresa.getCnaes() != null ? String.join(",", empresa.getCnaes()) : "N/A",
            empresa.getExperienciasTecnicas() != null && !empresa.getExperienciasTecnicas().isEmpty() 
                ? empresa.getExperienciasTecnicas().stream()
                    .map(e -> String.format("[%s]: %s", e.getEspecialidade(), e.getDetalheExperiencia()))
                    .reduce((a, b) -> a + " | " + b).get()
                : "Nenhuma experiência detalhada informada.",
            empresa.getDocumentosRegulares() != null ? String.join(", ", empresa.getDocumentosRegulares()) : "Nenhum documento informado",
            converter.getFormat()
        );

        try {
            log.info("Enviando bloco '{}' para IA...", nomeBloco);
            String respostaRaw = chatModel.call(promptCompleto);
            String jsonLimpo = extrairJson(respostaRaw);
            log.debug("Resposta JSON limpa da IA para {}: {}", nomeBloco, jsonLimpo);
            return converter.convert(jsonLimpo);
        } catch (Exception e) {
            LogPadrao.logErro(log, LogPadrao.EVENTO_ERRO_DIAGNOSTICO_BLOCO, "DiagnosticoMatchService.analisarBloco", "bloco", nomeBloco, e.getMessage(), e);
            return new DiagnosticoBlocoDTO("ERRO", List.of("Falha técnica no processamento"), "Ocorreu um erro ao interpretar a resposta da IA.", List.of(), "Erro de processamento.");
        }
    }

    private String gerarVereditoGeral(JsonNode detalhes, Empresa empresa) {
        String prompt = String.format(
            "Você é um consultor sênior em licitações. Com base nas análises técnicas abaixo, dê um veredito humanizado e direto para o dono da empresa %s.\n" +
            "Diga se vale a pena participar, quais os riscos reais e dê conselhos práticos.\n" +
            "Fale de forma amigável, como um consultor falando com um cliente.\n\n" +
            "RESUMO DAS ÁREAS:\n%s\n\n" +
            "ESCREVA UM PARÁGRAFO DE PARECER FINAL:",
            empresa.getRazaoSocial(), detalhes.toPrettyString()
        );

        try {
            return chatModel.call(prompt);
        } catch (Exception e) {
            LogPadrao.logErro(log, LogPadrao.EVENTO_ERRO_VEREDITO, "DiagnosticoMatchService.gerarVereditoGeral", "empresa", empresa.getRazaoSocial(), e.getMessage(), e);
            return "Não foi possível gerar o veredito automático: " + e.getMessage();
        }
    }

    private String extrairJson(String texto) {
        if (texto == null || texto.isBlank()) return "{}";
        String limpo = texto.replaceAll("```json", "").replaceAll("```", "").trim();
        int p = limpo.indexOf("{");
        int u = limpo.lastIndexOf("}");
        return (p >= 0 && u > p) ? limpo.substring(p, u + 1) : limpo;
    }
}
