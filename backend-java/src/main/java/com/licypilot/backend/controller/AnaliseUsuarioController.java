package com.licypilot.backend.controller;

import com.licypilot.backend.model.AnaliseUsuario;
import com.licypilot.backend.repository.AnaliseUsuarioRepository;
import com.licypilot.backend.util.LogPadrao;
import com.licypilot.backend.service.DiagnosticoMatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analises")
public class AnaliseUsuarioController {

    private static final Logger log = LoggerFactory.getLogger(AnaliseUsuarioController.class);
    private final AnaliseUsuarioRepository analiseUsuarioRepository;
    private final DiagnosticoMatchService diagnosticoMatchService;

    public AnaliseUsuarioController(AnaliseUsuarioRepository analiseUsuarioRepository, DiagnosticoMatchService diagnosticoMatchService) {
        this.analiseUsuarioRepository = analiseUsuarioRepository;
        this.diagnosticoMatchService = diagnosticoMatchService;
    }

    @GetMapping
    public ResponseEntity<List<AnaliseUsuario>> listar() {
        return ResponseEntity.ok(analiseUsuarioRepository.findAll());
    }

    @GetMapping("/licitacao/{licitacaoId}")
    public ResponseEntity<List<AnaliseUsuario>> buscarPorLicitacao(@PathVariable UUID licitacaoId) {
        return ResponseEntity.ok(analiseUsuarioRepository.findAll().stream()
                .filter(a -> a.getLicitacao().getId().equals(licitacaoId))
                .toList());
    }

    @PostMapping("/{analiseId}/diagnostico")
    public ResponseEntity<AnaliseUsuario> executarDiagnostico(@PathVariable UUID analiseId) {
        try {
            AnaliseUsuario analise = diagnosticoMatchService.executarDiagnosticoCompleto(analiseId);
            return ResponseEntity.ok(analise);
        } catch (Exception e) {
            LogPadrao.logErro(log, LogPadrao.EVENTO_ERRO_DIAGNOSTICO_API, "AnaliseUsuarioController.executarDiagnostico", "analiseId", analiseId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
