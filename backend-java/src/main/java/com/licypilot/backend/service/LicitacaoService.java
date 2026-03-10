package com.licypilot.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licypilot.backend.dto.ExtractionResponseDTO;
import com.licypilot.backend.dto.MasterJsonRecord;
import com.licypilot.backend.model.Licitacao;
import com.licypilot.backend.model.StatusProcessamento;
import com.licypilot.backend.repository.LicitacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    public Licitacao importarLicitacao(MultipartFile arquivo) {
        log.info("Recebendo arquivo: {}", arquivo.getOriginalFilename());
        
        Licitacao licitacao = Licitacao.builder()
                .numeroEdital("PROCESSANDO...")
                .statusProcessamento(StatusProcessamento.PROCESSANDO)
                .arquivoUrl(arquivo.getOriginalFilename())
                .build();
        
        licitacao = licitacaoRepository.save(licitacao);
        
        // Inicia o processamento pesado em background
        processarLicitacaoAsync(licitacao, arquivo);
        
        return licitacao;
    }

    public List<Licitacao> listarTodas() {
        return licitacaoRepository.findAll();
    }

    @Async
    public void processarLicitacaoAsync(Licitacao licitacao, MultipartFile arquivo) {
        try {
            log.info("Iniciando extração Python para licitação ID: {}", licitacao.getId());
            ExtractionResponseDTO extração = pythonClient.extrairTexto(arquivo.getResource());
            
            MasterJsonRecord acumulador = null;
            BeanOutputConverter<MasterJsonRecord> converter = new BeanOutputConverter<>(MasterJsonRecord.class);

            List<ExtractionResponseDTO.SectionSegmentDTO> segmentos = extração.segments();
            log.info("Iniciando processamento de {} segmentos do PDF...", segmentos.size());

            for (int i = 0; i < segmentos.size(); i++) {
                ExtractionResponseDTO.SectionSegmentDTO segmento = segmentos.get(i);
                log.info("Processando Bloco {}/{} (Página {}) via IA...", i + 1, segmentos.size(), segmento.pagina_inicio());

                try {
                    String instrucoes = "Você é um especialista em licitações brasileiras. Sua tarefa é extrair informações estruturadas do texto de um edital fornecido.\n\n" +
                            "REGRAS CRÍTICAS:\n" +
                            "1. Use APENAS as informações presentes no texto abaixo.\n" +
                            "2. Se não encontrar uma informação, retorne null para aquele campo.\n" +
                            "3. Extraia valores financeiros (R$) estritamente como números Double.\n" +
                            "4. Extraia prazos estritamente como números Inteiros.\n" +
                            "5. Capture o 'trecho_original' exato para comprovar cada exigência de habilitação ou risco.\n" +
                            "6. RETORNE APENAS O JSON PURO. NÃO ESCREVA NADA ANTES OU DEPOIS DO JSON.\n\n" +
                            "TEXTO DO EDITAL (Página " + segmento.pagina_inicio() + "):\n" +
                            segmento.texto_limpo() + "\n\n" +
                            "O formato de saída DEVE ser um JSON seguindo este esquema:\n" +
                            converter.getFormat();

                    String respostaRaw = chatModel.call(instrucoes);
                    log.debug("Resposta bruta da IA (Bloco {}): {}", i + 1, respostaRaw);
                    
                    String jsonLimpo = extrairJson(respostaRaw);
                    MasterJsonRecord parcial = converter.convert(jsonLimpo);
                    
                    acumulador = masterJsonMerger.merge(acumulador, parcial);
                } catch (Exception e) {
                    log.error("Erro ao processar bloco {} (Página {}): {}. Pulando para o próximo.", i + 1, segmento.pagina_inicio(), e.getMessage());
                    // Continua o loop para os próximos segmentos
                }
            }

            if (acumulador != null) {
                JsonNode jsonNode = objectMapper.valueToTree(acumulador);
                licitacao.setMasterJson(jsonNode);
                
                if (acumulador.identificacaoProjeto() != null) {
                    licitacao.setNumeroEdital(acumulador.identificacaoProjeto().numero_edital());
                    licitacao.setObjeto(acumulador.identificacaoProjeto().objeto_completo());
                    licitacao.setOrgaoEmissor(acumulador.identificacaoProjeto().orgao_emissor());
                }

                if (acumulador.prazosValoresPagamento() != null) {
                    licitacao.setValorEstimado(acumulador.prazosValoresPagamento().valor_estimado_total());
                }
            }
            
            licitacao.setStatusProcessamento(StatusProcessamento.CONCLUIDO);
            licitacaoRepository.save(licitacao);
            log.info("Processamento concluído com sucesso para licitação ID: {}", licitacao.getId());

            // Gatilho de Viabilidade (Fase 4)
            viabilidadeService.processarViabilidadeInicial(licitacao);

        } catch (Exception e) {
            log.error("Erro no processamento assíncrono: ", e);
            licitacao.setStatusProcessamento(StatusProcessamento.TIMEOUT_IA);
            licitacao.setObservacoesErro(e.getMessage() != null ? e.getMessage() : e.toString());
            licitacaoRepository.save(licitacao);
        }
    }

    private String extrairJson(String texto) {
        if (texto == null || texto.isBlank()) return "{}";
        String limpo = texto.replaceAll("```json", "").replaceAll("```", "").trim();
        int primeiroBrace = limpo.indexOf("{");
        int ultimoBrace = limpo.lastIndexOf("}");
        if (primeiroBrace >= 0 && ultimoBrace > primeiroBrace) {
            return limpo.substring(primeiroBrace, ultimoBrace + 1);
        }
        return limpo;
    }
}
