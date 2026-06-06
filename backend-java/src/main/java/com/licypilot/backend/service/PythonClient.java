package com.licypilot.backend.service;

import com.licypilot.backend.dto.ExtractionResponseDTO;
import com.licypilot.backend.util.LogPadrao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PythonClient {

    private static final Logger log = LoggerFactory.getLogger(PythonClient.class);
    private final RestClient restClient;

    public PythonClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8000")
                .build();
    }

    public ExtractionResponseDTO extrairTexto(Resource arquivo) {
        return extrairTexto(arquivo, null);
    }

    public ExtractionResponseDTO extrairTexto(Resource arquivo, Integer maxPages) {
        String nomeArquivo = arquivo != null ? arquivo.getFilename() : "n/a";
        log.info("Chamando extrator Python: arquivo={} maxPages={}", nomeArquivo, maxPages);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", arquivo)
                .filename(arquivo.getFilename())
                .contentType(MediaType.APPLICATION_PDF);

        try {
            return restClient.post()
                .uri(uriBuilder -> {
                    uriBuilder.path("/extract");
                    if (maxPages != null) {
                        uriBuilder.queryParam("max_pages", maxPages);
                    }
                    return uriBuilder.build();
                })
                // ? 
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(ExtractionResponseDTO.class);
        } catch (Exception e) {
            //o throw e é para que o erro seja propagado para o controller
            //o LogPadrao.logErro é um metodo que loga o erro em um arquivo de log que é gerado pelo Spring Boot e é utilizado para monitorar o sistema
            //o LogPadrao é uma classe que contém constantes para os eventos de log que eu defini no arquivo LogPadrao.java para facilitar a leitura e manutenção do codigo 
            LogPadrao.logErro(log, LogPadrao.EVENTO_ERRO_PYTHON_EXTRATOR, "PythonClient.extrairTexto", "arquivo", nomeArquivo, e.getMessage(), e);
            throw e;
        }
    }
}
