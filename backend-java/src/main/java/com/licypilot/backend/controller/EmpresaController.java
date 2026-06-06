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
    private final com.licypilot.backend.service.DemonstrationService demonstrationService;

    public EmpresaController(EmpresaRepository empresaRepository, com.licypilot.backend.service.DemonstrationService demonstrationService) {
        this.empresaRepository = empresaRepository;
        this.demonstrationService = demonstrationService;
    }

    @PostMapping
    public ResponseEntity<Empresa> cadastrar(@RequestBody Empresa empresa) {
        return ResponseEntity.ok(empresaRepository.save(empresa));
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> listar() {
        return ResponseEntity.ok(empresaRepository.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> atualizar(@PathVariable java.util.UUID id, @RequestBody Empresa dados) {
        return empresaRepository.findById(id)
                .map(empresa -> {
                    empresa.setRazaoSocial(dados.getRazaoSocial());
                    empresa.setCnpj(dados.getCnpj());
                    empresa.setCapitalSocial(dados.getCapitalSocial());
                    empresa.setPorte(dados.getPorte());
                    empresa.setCnaes(dados.getCnaes());
                    empresa.setDocumentosRegulares(dados.getDocumentosRegulares());
                    empresa.setExperienciasTecnicas(dados.getExperienciasTecnicas());
                    return ResponseEntity.ok(empresaRepository.save(empresa));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetarEmpresas() {
        demonstrationService.resetFull();
        return ResponseEntity.noContent().build();
    }
}
