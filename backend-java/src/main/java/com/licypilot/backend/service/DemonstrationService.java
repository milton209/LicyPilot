package com.licypilot.backend.service;

import com.licypilot.backend.repository.AnaliseUsuarioRepository;
import com.licypilot.backend.repository.EmpresaRepository;
import com.licypilot.backend.repository.LicitacaoRepository;
import com.licypilot.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemonstrationService {

    private static final Logger log = LoggerFactory.getLogger(DemonstrationService.class);
    
    private final AnaliseUsuarioRepository analiseUsuarioRepository;
    private final LicitacaoRepository licitacaoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public DemonstrationService(AnaliseUsuarioRepository analiseUsuarioRepository,
                                LicitacaoRepository licitacaoRepository,
                                EmpresaRepository empresaRepository,
                                UsuarioRepository usuarioRepository) {
        this.analiseUsuarioRepository = analiseUsuarioRepository;
        this.licitacaoRepository = licitacaoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void resetAnalises() {
        log.warn("Limpando todas as análises...");
        analiseUsuarioRepository.deleteAll();
    }

    @Transactional
    public void resetLicitacoes() {
        log.warn("Limpando licitações e análises (preservando empresas e usuários)...");
        analiseUsuarioRepository.deleteAll();
        licitacaoRepository.deleteAll();
        log.info("Reset de licitações concluído.");
    }

    @Transactional
    public void resetFull() {
        log.warn("Executando reset TOTAL do sistema...");
        analiseUsuarioRepository.deleteAll();
        licitacaoRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        log.info("Reset TOTAL concluído.");
    }
}
