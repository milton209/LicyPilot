package com.licypilot.backend.service;

import com.licypilot.backend.dto.ExtractionResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PythonClient {

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
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", arquivo)
                .filename(arquivo.getFilename())
                .contentType(MediaType.APPLICATION_PDF);

        return restClient.post()
                .uri(uriBuilder -> {
                    uriBuilder.path("/extract");
                    if (maxPages != null) {
                        uriBuilder.queryParam("max_pages", maxPages);
                    }
                    return uriBuilder.build();
                })
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(ExtractionResponseDTO.class);
    }
}
