package com.licypilot.backend.controller;

import com.licypilot.backend.model.Empresa;
import com.licypilot.backend.repository.EmpresaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private final EmpresaRepository empresaRepository;

    public EmpresaController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @PostMapping
    public ResponseEntity<Empresa> cadastrar(@RequestBody Empresa empresa) {
        return ResponseEntity.ok(empresaRepository.save(empresa));
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> listar() {
        return ResponseEntity.ok(empresaRepository.findAll());
    }
}
