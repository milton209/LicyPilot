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

    public LicitacaoController(LicitacaoService licitacaoService) {
        this.licitacaoService = licitacaoService;
    }

    @PostMapping("/importar")
    public ResponseEntity<Licitacao> importar(@RequestParam("arquivo") MultipartFile arquivo,
                                             @RequestParam(value = "maxPages", required = false) Integer maxPages,
                                             @RequestParam(value = "reprocessar", defaultValue = "false") boolean reprocessar) {
        Licitacao licitacao = licitacaoService.importarLicitacao(arquivo, maxPages, reprocessar);
        return ResponseEntity.accepted().body(licitacao);
    }
    @GetMapping
    public ResponseEntity<List<Licitacao>> listarTodos() {
        return ResponseEntity.ok(licitacaoService.listarTodas());
    }
}
