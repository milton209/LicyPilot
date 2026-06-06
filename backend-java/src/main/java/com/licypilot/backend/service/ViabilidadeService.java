package com.licypilot.backend.service;

import com.licypilot.backend.model.*;
import com.licypilot.backend.repository.AnaliseUsuarioRepository;
import com.licypilot.backend.repository.EmpresaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViabilidadeService {

    private static final Logger log = LoggerFactory.getLogger(ViabilidadeService.class);
    private final EmpresaRepository empresaRepository;
    private final AnaliseUsuarioRepository analiseUsuarioRepository;

    public ViabilidadeService(EmpresaRepository empresaRepository, AnaliseUsuarioRepository analiseUsuarioRepository) {
        this.empresaRepository = empresaRepository;
        this.analiseUsuarioRepository = analiseUsuarioRepository;
    }

    public void processarViabilidadeInicial(Licitacao licitacao) {
        log.info("Extração do Master JSON concluída para edital: {}. Agora ele está disponível para análise dos usuários.", licitacao.getId());
        // Removido o loop automático que criava análises para todas as empresas.
        // O usuário agora cria a análise sob demanda no frontend.
    }

    private StatusViabilidade calcularStatusInicial(Licitacao licitacao, Empresa empresa) {
        if (licitacao.getValorEstimado() == null || empresa.getCapitalSocial() == null) {
            return StatusViabilidade.REVISAO_MANUAL;
        }

        // Regra de exemplo: Capital Social deve ser pelo menos 10% do valor estimado (comum em editais)
        double minimoExigido = licitacao.getValorEstimado() * 0.10;
        
        if (empresa.getCapitalSocial() >= minimoExigido) {
            return StatusViabilidade.ALTA;
        } else if (empresa.getCapitalSocial() >= licitacao.getValorEstimado() * 0.05) {
            return StatusViabilidade.BAIXA;
        } else {
            return StatusViabilidade.INCOMPATIVEL;
        }
    }
}
