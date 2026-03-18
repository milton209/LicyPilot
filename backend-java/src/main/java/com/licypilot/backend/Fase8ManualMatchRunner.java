package com.licypilot.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licypilot.backend.model.*;
import com.licypilot.backend.repository.AnaliseUsuarioRepository;
import com.licypilot.backend.repository.EmpresaRepository;
import com.licypilot.backend.repository.LicitacaoRepository;
import com.licypilot.backend.service.DiagnosticoMatchService;
import com.licypilot.backend.service.ViabilidadeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@Component
@Profile("manual-match")
public class Fase8ManualMatchRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Fase8ManualMatchRunner.class);
    
    private final EmpresaRepository empresaRepository;
    private final LicitacaoRepository licitacaoRepository;
    private final ViabilidadeService viabilidadeService;
    private final DiagnosticoMatchService diagnosticoMatchService;
    private final AnaliseUsuarioRepository analiseUsuarioRepository;
    private final ObjectMapper objectMapper;

    public Fase8ManualMatchRunner(EmpresaRepository empresaRepository, 
                                  LicitacaoRepository licitacaoRepository,
                                  ViabilidadeService viabilidadeService, 
                                  DiagnosticoMatchService diagnosticoMatchService, 
                                  AnaliseUsuarioRepository analiseUsuarioRepository,
                                  ObjectMapper objectMapper) {
        this.empresaRepository = empresaRepository;
        this.licitacaoRepository = licitacaoRepository;
        this.viabilidadeService = viabilidadeService;
        this.diagnosticoMatchService = diagnosticoMatchService;
        this.analiseUsuarioRepository = analiseUsuarioRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info(">>>> INICIANDO FASE 8: TESTE DE MATCH COM JSON MANUAL <<<<");

        // 1. Preparar Empresa (CNPJ novo para teste limpo)
        Empresa empresa = criarEmpresa("LicyPilot Test Clean", "22.222.222/0002-22", 1000000.0, List.of("6201-5/00"));

        // 2. Injetar JSON Master manualmente
        log.info("Lendo JSONmaster.txt para injeção direta...");
        File masterJsonFile = new File("..\\EditalLicitaçãoTeste\\JSONmaster.txt");
        if (!masterJsonFile.exists()) {
            log.error("Arquivo JSONmaster.txt não encontrado em: {}", masterJsonFile.getAbsolutePath());
            return;
        }

        String jsonContent = Files.readString(masterJsonFile.toPath());
        JsonNode rootNode = objectMapper.readTree(jsonContent);

        // 3. Criar ou atualizar Licitação com o JSON Manual (Hash novo para teste limpo)
        String hash = "manual-match-test-002";
        Licitacao licitacao = licitacaoRepository.findByArquivoHash(hash).orElseGet(() -> {
            Licitacao newLic = new Licitacao();
            newLic.setArquivoHash(hash);
            newLic.setArquivoUrl("EDITAL20263.pdf");
            return newLic;
        });

        licitacao.setMasterJson(rootNode);
        licitacao.setStatusProcessamento(StatusProcessamento.CONCLUIDO);
        
        if (rootNode.has("identificacao_projeto")) {
            JsonNode idProj = rootNode.get("identificacao_projeto");
            licitacao.setNumeroEdital(idProj.path("numero_edital").asText());
            licitacao.setOrgaoEmissor(idProj.path("orgao_emissor").asText());
            licitacao.setObjeto(idProj.path("objeto_completo").asText());
        }
        
        if (rootNode.has("prazos_e_valores")) {
            licitacao.setValorEstimado(rootNode.get("prazos_e_valores").path("valor_estimado_total").asDouble());
        } else if (rootNode.has("prazos_valores_e_pagamento")) {
            licitacao.setValorEstimado(rootNode.get("prazos_valores_e_pagamento").path("valor_estimado_total").asDouble());
        }

        licitacao = licitacaoRepository.save(licitacao);
        log.info("Licitação preparada com ID: {}", licitacao.getId());

        // 4. Rodar Viabilidade Inicial
        viabilidadeService.processarViabilidadeInicial(licitacao);

        // 5. Rodar Diagnóstico de Match
        UUID licitacaoId = licitacao.getId();
        AnaliseUsuario analise = analiseUsuarioRepository.findByLicitacaoAndEmpresa(licitacao, empresa)
                .orElseThrow(() -> new RuntimeException("Falha ao gerar análise inicial."));

        log.info("Iniciando Diagnóstico de Match (Chamando IA para Veredito Final)...");
        AnaliseUsuario resultado = diagnosticoMatchService.executarDiagnosticoCompleto(analise.getId());
        
        if (resultado.getDiagnosticoJson() != null) {
            log.info(">>>> DIAGNÓSTICO CONCLUÍDO COM SUCESSO <<<<");
            log.info("VEREDITO DO ESPECIALISTA:\n{}", resultado.getDiagnosticoJson().path("veredito_do_especialista").asText());
        }

        log.info(">>>> FIM DA FASE 8 <<<<");
    }

    private Empresa criarEmpresa(String nome, String cnpj, Double capital, List<String> cnaes) {
        return empresaRepository.findByCnpj(cnpj).orElseGet(() -> {
            Empresa e = new Empresa();
            e.setRazaoSocial(nome);
            e.setCnpj(cnpj);
            e.setCapitalSocial(capital);
            e.setCnaes(cnaes);
            return empresaRepository.save(e);
        });
    }
}
