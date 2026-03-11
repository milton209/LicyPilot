package com.licypilot.backend;

import com.licypilot.backend.model.AnaliseUsuario;
import com.licypilot.backend.model.Empresa;
import com.licypilot.backend.model.Licitacao;
import com.licypilot.backend.repository.AnaliseUsuarioRepository;
import com.licypilot.backend.repository.EmpresaRepository;
import com.licypilot.backend.repository.LicitacaoRepository;
import com.licypilot.backend.service.DiagnosticoMatchService;
import com.licypilot.backend.service.ViabilidadeService;
import com.licypilot.backend.util.TestDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("teste")
public class Fase6Runner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Fase6Runner.class);
    
    private final EmpresaRepository empresaRepository;
    private final LicitacaoRepository licitacaoRepository;
    private final ViabilidadeService viabilidadeService;
    private final DiagnosticoMatchService diagnosticoMatchService;
    private final AnaliseUsuarioRepository analiseUsuarioRepository;

    public Fase6Runner(EmpresaRepository empresaRepository, 
                       LicitacaoRepository licitacaoRepository, 
                       ViabilidadeService viabilidadeService, 
                       DiagnosticoMatchService diagnosticoMatchService, 
                       AnaliseUsuarioRepository analiseUsuarioRepository) {
        this.empresaRepository = empresaRepository;
        this.licitacaoRepository = licitacaoRepository;
        this.viabilidadeService = viabilidadeService;
        this.diagnosticoMatchService = diagnosticoMatchService;
        this.analiseUsuarioRepository = analiseUsuarioRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info(">>>> INICIANDO FASE 6: INTERFACE DE TESTE E VALIDAÇÃO <<<<");

        // 1. PREPARAR DADOS (Empresa e Edital)
        Empresa empresa = prepararEmpresa();
        Licitacao licitacao = prepararLicitacao();

        // 2. EXECUTAR VIABILIDADE INICIAL (Fase 4 - Cálculo Rápido)
        viabilidadeService.processarViabilidadeInicial(licitacao);
        
        AnaliseUsuario analise = analiseUsuarioRepository.findByLicitacaoAndEmpresa(licitacao, empresa)
                .orElseThrow(() -> new RuntimeException("Falha ao gerar análise inicial no banco."));

        // 3. EXECUTAR DIAGNÓSTICO DE MATCH (Fase 5 - IA com Paralelismo)
        executarDiagnosticoIA(analise);

        log.info(">>>> FIM DA VALIDAÇÃO DO SISTEMA <<<<");
    }

    private Empresa prepararEmpresa() {
        return empresaRepository.findAll().stream().findFirst().orElseGet(() -> {
            log.info("[TESTE] Nenhuma empresa no banco. Criando mock via TestDataProvider...");
            return empresaRepository.save(TestDataProvider.criarEmpresaExemplo());
        });
    }

    private Licitacao prepararLicitacao() {
        // Busca se existe alguma licitação já com MasterJson (processada pelo Python)
        return licitacaoRepository.findAll().stream()
                .filter(l -> l.getMasterJson() != null && !l.getMasterJson().isEmpty())
                .findFirst()
                .orElseGet(() -> {
                    log.info("[TESTE] Nenhuma licitação processada encontrada. Injetando Mock MasterJson para teste da Fase 5.");
                    Licitacao mock = TestDataProvider.criarLicitacaoComMasterJson();
                    return licitacaoRepository.save(mock);
                });
    }

    private void executarDiagnosticoIA(AnaliseUsuario analise) {
        log.info("[TESTE] Iniciando Diagnóstico de Match (Chamando Ollama)...");
        long inicio = System.currentTimeMillis();
        
        try {
            AnaliseUsuario resultado = diagnosticoMatchService.executarDiagnosticoCompleto(analise.getId());
            long fim = System.currentTimeMillis();
            
            log.info("[TESTE] Diagnóstico finalizado em {}ms", (fim - inicio));
            if (resultado.getDiagnosticoJson() != null) {
                log.info("[TESTE] RESULTADO DA IA:\n{}", resultado.getDiagnosticoJson().toPrettyString());
            }
        } catch (Exception e) {
            log.error("[TESTE] ERRO CRÍTICO NO DIAGNÓSTICO: {}", e.getMessage());
        }
    }
}
