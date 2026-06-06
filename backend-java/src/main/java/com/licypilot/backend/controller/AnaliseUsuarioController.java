package com.licypilot.backend.controller;

import com.licypilot.backend.model.AnaliseUsuario;
import com.licypilot.backend.repository.AnaliseUsuarioRepository;
import com.licypilot.backend.util.LogPadrao;
import com.licypilot.backend.service.DiagnosticoMatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analises")
public class AnaliseUsuarioController {

    private static final Logger log = LoggerFactory.getLogger(AnaliseUsuarioController.class);
    private final AnaliseUsuarioRepository analiseUsuarioRepository;
    private final DiagnosticoMatchService diagnosticoMatchService;
    private final com.licypilot.backend.service.DemonstrationService demonstrationService;

    public AnaliseUsuarioController(AnaliseUsuarioRepository analiseUsuarioRepository, 
                                   DiagnosticoMatchService diagnosticoMatchService,
                                   com.licypilot.backend.service.DemonstrationService demonstrationService) {
        this.analiseUsuarioRepository = analiseUsuarioRepository;
        this.diagnosticoMatchService = diagnosticoMatchService;
        this.demonstrationService = demonstrationService;
    }

    @GetMapping
    public ResponseEntity<List<AnaliseUsuario>> listar() {
        return ResponseEntity.ok(analiseUsuarioRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnaliseUsuario> buscarPorId(@PathVariable UUID id) {
        return analiseUsuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/licitacao/{licitacaoId}")
    public ResponseEntity<List<AnaliseUsuario>> buscarPorLicitacao(@PathVariable UUID licitacaoId) {
        return ResponseEntity.ok(analiseUsuarioRepository.findAll().stream()
                .filter(a -> a.getLicitacao().getId().equals(licitacaoId))
                .toList());
    }

    @PostMapping("/iniciar")
    public ResponseEntity<AnaliseUsuario> iniciarAnalise(@RequestParam UUID licitacaoId, @RequestParam UUID empresaId) {
        return analiseUsuarioRepository.findByLicitacaoIdAndEmpresaId(licitacaoId, empresaId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    return ResponseEntity.ok(diagnosticoMatchService.criarAnaliseUsuario(licitacaoId, empresaId));
                });
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

    @GetMapping(value = "/{analiseId}/diagnostico/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamDiagnostico(@PathVariable UUID analiseId) {
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(180000L); // 3 minutos timeout
        try {
            diagnosticoMatchService.executarDiagnosticoSse(analiseId, emitter);
            return emitter;
        } catch (Exception e) {
            log.error("Erro ao iniciar stream SSE: {}", e.getMessage());
            emitter.completeWithError(e);
            return emitter;
        }
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetarAnalises() {
        demonstrationService.resetAnalises();
        return ResponseEntity.noContent().build();
    }
}
