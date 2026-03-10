package com.licypilot.backend.controller;

import com.licypilot.backend.model.AnaliseUsuario;
import com.licypilot.backend.repository.AnaliseUsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analises")
public class AnaliseUsuarioController {

    private final AnaliseUsuarioRepository analiseUsuarioRepository;

    public AnaliseUsuarioController(AnaliseUsuarioRepository analiseUsuarioRepository) {
        this.analiseUsuarioRepository = analiseUsuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<AnaliseUsuario>> listar() {
        return ResponseEntity.ok(analiseUsuarioRepository.findAll());
    }

    @GetMapping("/licitacao/{licitacaoId}")
    public ResponseEntity<List<AnaliseUsuario>> buscarPorLicitacao(@PathVariable UUID licitacaoId) {
        // Simplificado: Buscaria por objeto licitacao se necessário
        return ResponseEntity.ok(analiseUsuarioRepository.findAll().stream()
                .filter(a -> a.getLicitacao().getId().equals(licitacaoId))
                .toList());
    }
}
