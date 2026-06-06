package com.licypilot.backend.controller;

import com.licypilot.backend.model.Licitacao;
import com.licypilot.backend.service.LicitacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/licitacoes")
public class LicitacaoController {

    private final LicitacaoService licitacaoService;
    private final com.licypilot.backend.service.DemonstrationService demonstrationService;

    public LicitacaoController(LicitacaoService licitacaoService, com.licypilot.backend.service.DemonstrationService demonstrationService) {
        this.licitacaoService = licitacaoService;
        this.demonstrationService = demonstrationService;
    }

    @PostMapping("/importar")
    public ResponseEntity<Licitacao> importar(@RequestParam("arquivo") MultipartFile arquivo,
                                             @RequestParam(value = "titulo", required = false) String titulo,
                                             @RequestParam(value = "orgao", required = false) String orgao,
                                             @RequestParam(value = "maxPages", required = false) Integer maxPages,
                                             @RequestParam(value = "reprocessar", defaultValue = "false") boolean reprocessar) {
        Licitacao licitacao = licitacaoService.importarLicitacao(arquivo, titulo, orgao, maxPages, reprocessar);
        return ResponseEntity.accepted().body(licitacao);
    }
    @GetMapping
    public ResponseEntity<List<Licitacao>> listarTodos() {
        return ResponseEntity.ok(licitacaoService.listarTodas());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> servePdf(@PathVariable java.util.UUID id) {
        return licitacaoService.buscarPorId(id)
                .map(lic -> ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + lic.getArquivoUrl() + "\"")
                        .body(lic.getArquivoConteudo()))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetarLicitacoes() {
        demonstrationService.resetLicitacoes();
        return ResponseEntity.noContent().build();
    }
}
