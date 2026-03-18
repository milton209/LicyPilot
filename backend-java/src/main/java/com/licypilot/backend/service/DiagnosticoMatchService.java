package com.licypilot.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.licypilot.backend.dto.DiagnosticoBlocoDTO;
import com.licypilot.backend.model.AnaliseUsuario;
import com.licypilot.backend.model.Empresa;
import com.licypilot.backend.repository.AnaliseUsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class DiagnosticoMatchService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticoMatchService.class);
    private final AnaliseUsuarioRepository analiseUsuarioRepository;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public DiagnosticoMatchService(AnaliseUsuarioRepository analiseUsuarioRepository, ChatModel chatModel, ObjectMapper objectMapper) {
        this.analiseUsuarioRepository = analiseUsuarioRepository;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
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

        // 1. Analisa as áreas técnicas em paralelo (isso é rápido e não afeta o contexto global)
        List<CompletableFuture<Void>> tarefas = new ArrayList<>();
        tarefas.add(agendarAnaliseBloco(blocosTecnicos, "habilitacao", "Habilitação", masterJson.path("habilitacao_detalhada"), empresa));
        tarefas.add(agendarAnaliseBloco(blocosTecnicos, "qualificacao_tecnica", "Qualificação Técnica", masterJson.path("qualificacao_tecnica_especifica"), empresa));
        tarefas.add(agendarAnaliseBloco(blocosTecnicos, "financeiro", "Financeiro", masterJson.path("prazos_valores_e_pagamento"), empresa));
        tarefas.add(agendarAnaliseBloco(blocosTecnicos, "regras_e_riscos", "Regras e Riscos", masterJson.path("regras_da_disputa"), empresa));

        CompletableFuture.allOf(tarefas.toArray(new CompletableFuture[0])).join();

        // 2. Adiciona os blocos ao JSON final
        diagnosticoFinal.set("detalhes_por_area", blocosTecnicos);

        // 3. GERA O VEREDITO HUMANIZADO GERAL (Abrangendo tudo)
        log.info("Gerando Veredito Final Humanizado...");
        String vereditoGeral = gerarVereditoGeral(blocosTecnicos, empresa);
        diagnosticoFinal.put("veredito_do_especialista", vereditoGeral);

        analise.setDiagnosticoJson(diagnosticoFinal);
        return analiseUsuarioRepository.save(analise);
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
            "Com os dados da Empresa: Capital R$ %.2f, CNAEs %s.\n\n" +
            "### FORMATO DE RESPOSTA ESPERADO ###\n" +
            "%s",
            nomeBloco, empresa.getRazaoSocial(), dadosEdital.toString(), empresa.getCapitalSocial(), String.join(",", empresa.getCnaes()), converter.getFormat()
        );

        try {
            log.info("Enviando bloco '{}' para IA...", nomeBloco);
            String respostaRaw = chatModel.call(promptCompleto);
            String jsonLimpo = extrairJson(respostaRaw);
            log.debug("Resposta JSON limpa da IA para {}: {}", nomeBloco, jsonLimpo);
            return converter.convert(jsonLimpo);
        } catch (Exception e) {
            log.error("Erro ao analisar bloco '{}': {}", nomeBloco, e.getMessage());
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
