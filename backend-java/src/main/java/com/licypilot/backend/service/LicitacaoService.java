package com.licypilot.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licypilot.backend.dto.ExtractionResponseDTO;
import com.licypilot.backend.dto.MasterJsonRecord;
import com.licypilot.backend.model.Licitacao;
import com.licypilot.backend.model.StatusProcessamento;
import com.licypilot.backend.repository.LicitacaoRepository;
import com.licypilot.backend.util.LogPadrao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class LicitacaoService {

    private static final Logger log = LoggerFactory.getLogger(LicitacaoService.class);
    private final LicitacaoRepository licitacaoRepository;
    private final PythonClient pythonClient;
    private final MasterJsonMerger masterJsonMerger;
    private final ViabilidadeService viabilidadeService;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public LicitacaoService(LicitacaoRepository licitacaoRepository, 
                            PythonClient pythonClient, 
                            MasterJsonMerger masterJsonMerger,
                            ViabilidadeService viabilidadeService,
                            ChatModel chatModel,
                            ObjectMapper objectMapper) {
        this.licitacaoRepository = licitacaoRepository;
        this.pythonClient = pythonClient;
        this.masterJsonMerger = masterJsonMerger;
        this.viabilidadeService = viabilidadeService;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    public List<Licitacao> listarTodas() {
        return licitacaoRepository.findAll();
    }

    public Optional<Licitacao> buscarPorId(java.util.UUID id) {
        return licitacaoRepository.findById(id);
    }

    public Licitacao importarLicitacao(MultipartFile arquivo, Integer maxPages, boolean reprocessar) {
        try {
            byte[] conteudo = arquivo.getBytes();
            String hash = gerarHash(conteudo);

            log.info("Recebendo arquivo: {}. Hash: {}. Reprocessar: {}", arquivo.getOriginalFilename(), hash, reprocessar);

            Optional<Licitacao> existente = licitacaoRepository.findByArquivoHash(hash);
            if (existente.isPresent()) {
                if (reprocessar) {
                    log.info("Forçando reprocessamento de edital existente...");
                    Licitacao lic = existente.get();
                    lic.setStatusProcessamento(StatusProcessamento.PROCESSANDO);
                    lic.setMasterJson(null);
                    lic.setObservacoesErro(null);
                    lic = licitacaoRepository.save(lic);
                    processarLicitacaoAsync(lic, arquivo, maxPages);
                    return lic;
                }
                log.warn("Edital duplicado detectado. Retornando existente.");
                return existente.get();
            }

            Licitacao licitacao = Licitacao.builder()
                    .numeroEdital("PROCESSANDO...")
                    .statusProcessamento(StatusProcessamento.PROCESSANDO)
                    .arquivoUrl(arquivo.getOriginalFilename())
                    .arquivoConteudo(conteudo)
                    .arquivoHash(hash)
                    .build();
            
            licitacao = licitacaoRepository.save(licitacao);
            processarLicitacaoAsync(licitacao, arquivo, maxPages);
            
            return licitacao;
        } catch (Exception e) {
            LogPadrao.logErro(log, LogPadrao.EVENTO_ERRO_IMPORTACAO, "LicitacaoService.importarLicitacao", "arquivo", arquivo.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("Falha no upload: " + e.getMessage());
        }
    }

    private String gerarHash(byte[] conteudo) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(conteudo);
        return HexFormat.of().formatHex(encodedhash);
    }

    @Async
    public void processarLicitacaoAsync(Licitacao licitacao, MultipartFile arquivo, Integer maxPages) {
        try {
            log.info("Iniciando extração sequencial para ID: {}", licitacao.getId());
            ExtractionResponseDTO extração = pythonClient.extrairTexto(arquivo.getResource(), maxPages);
            List<ExtractionResponseDTO.SectionSegmentDTO> segmentos = extração.segments();
            
            MasterJsonRecord acumuladorGlobal = null;
            BeanOutputConverter<MasterJsonRecord> converter = new BeanOutputConverter<>(MasterJsonRecord.class);
            String contextoAnterior = "";

            for (ExtractionResponseDTO.SectionSegmentDTO segmento : segmentos) {
                log.info("Processando página {} de {}...", segmento.pagina_inicio(), segmentos.size());
                
                String instrucoes = "Você é um especialista em licitações. Extraia os dados da página abaixo em JSON PURO.\n";
                if (!contextoAnterior.isEmpty()) {
                    instrucoes += "CONTEXTO DA PÁGINA ANTERIOR (resumo): " + contextoAnterior + "\n\n";
                }
                instrucoes += "TEXTO DA PÁGINA ATUAL:\n" + segmento.texto_limpo() + "\n\n" +
                             "RETORNE APENAS O JSON NO ESQUEMA: " + converter.getFormat();

                String respostaRaw = chatModel.call(instrucoes);
                MasterJsonRecord parcial = converter.convert(extrairJson(respostaRaw));
                
                // Mescla os dados da página atual com o que já foi extraído
                acumuladorGlobal = masterJsonMerger.merge(acumuladorGlobal, parcial);
                
                // Atualiza contexto para a próxima página (últimos 800 caracteres)
                contextoAnterior = segmento.texto_limpo();
                if (contextoAnterior.length() > 800) {
                    contextoAnterior = contextoAnterior.substring(contextoAnterior.length() - 800);
                }
            }

            if (acumuladorGlobal != null) {
                licitacao.setMasterJson(objectMapper.valueToTree(acumuladorGlobal));
                if (acumuladorGlobal.identificacaoProjeto() != null) {
                    licitacao.setNumeroEdital(acumuladorGlobal.identificacaoProjeto().numero_edital());
                    licitacao.setObjeto(acumuladorGlobal.identificacaoProjeto().objeto_completo());
                    licitacao.setOrgaoEmissor(acumuladorGlobal.identificacaoProjeto().orgao_emissor());
                }
                if (acumuladorGlobal.prazosValoresPagamento() != null) {
                    licitacao.setValorEstimado(acumuladorGlobal.prazosValoresPagamento().valor_estimado_total());
                }
            }
            
            licitacao.setStatusProcessamento(StatusProcessamento.CONCLUIDO);
            licitacaoRepository.save(licitacao);
            log.info("Extração concluída com sucesso para ID: {}", licitacao.getId());

            viabilidadeService.processarViabilidadeInicial(licitacao);

        } catch (Exception e) {
            String ctx = String.format("licitacaoId=%s arquivo=%s", licitacao.getId(), licitacao.getArquivoUrl());
            LogPadrao.logErro(log, LogPadrao.EVENTO_ERRO_PROCESSAMENTO, "LicitacaoService.processarLicitacaoAsync", ctx, e.getMessage(), e);
            licitacao.setStatusProcessamento(StatusProcessamento.TIMEOUT_IA);
            licitacaoRepository.save(licitacao);
        }
    }

    private String extrairJson(String texto) {
        if (texto == null || texto.isBlank()) return "{}";
        // Remove blocos de markdown se existirem
        String limpo = texto.replaceAll("```json", "").replaceAll("```", "").trim();
        
        // Localiza o primeiro '{' e o último '}' para garantir que temos apenas o objeto JSON
        int primeiroBrace = limpo.indexOf("{");
        int ultimoBrace = limpo.lastIndexOf("}");
        
        if (primeiroBrace >= 0 && ultimoBrace > primeiroBrace) {
            return limpo.substring(primeiroBrace, ultimoBrace + 1);
        }
        
        return limpo;
    }
}
