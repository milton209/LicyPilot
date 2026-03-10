package com.licypilot.backend.controller;

import com.licypilot.backend.model.Licitacao;
import com.licypilot.backend.model.StatusProcessamento;
import com.licypilot.backend.service.LicitacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LicitacaoController.class)
public class LicitacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LicitacaoService licitacaoService;

    @Test
    public void deveImportarLicitacaoComSucesso() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "edital.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "conteudo do pdf".getBytes()
        );

        Licitacao licitacaoMock = Licitacao.builder()
                .id(UUID.randomUUID())
                .numeroEdital("PENDENTE")
                .statusProcessamento(StatusProcessamento.PROCESSANDO)
                .arquivoUrl("edital.pdf")
                .build();

        when(licitacaoService.importarLicitacao(any())).thenReturn(licitacaoMock);

        mockMvc.perform(multipart("/api/v1/licitacoes/importar")
                        .file(arquivo))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.statusProcessamento").value("PROCESSANDO"))
                .andExpect(jsonPath("$.arquivoUrl").value("edital.pdf"));
    }
}
