package com.licypilot.backend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licypilot.backend.model.Empresa;
import com.licypilot.backend.model.Licitacao;
import com.licypilot.backend.model.StatusProcessamento;

import java.util.List;
import java.util.UUID;

/**
 * Utilitário para centralizar a criação de dados de teste (Mocks).
 * Facilita a manutenção: se o modelo mudar, alteramos apenas aqui.
 */
public class TestDataProvider {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Empresa criarEmpresaExemplo() {
        Empresa empresa = new Empresa();
        empresa.setCnpj("12.345.678/0001-90");
        empresa.setRazaoSocial("LicyTech Soluções em TI");
        empresa.setCapitalSocial(500000.00);
        empresa.setCnaes(List.of("6201-5/00", "6202-3/00"));
        return empresa;
    }

    public static Licitacao criarLicitacaoComMasterJson() {
        Licitacao licitacao = new Licitacao();
        licitacao.setNumeroEdital("019/2026");
        licitacao.setOrgaoEmissor("Prefeitura de Teste");
        licitacao.setValorEstimado(1000000.00);
        licitacao.setStatusProcessamento(StatusProcessamento.CONCLUIDO);
        
        // Simulação de um Master JSON estruturado (Fase 4 concluída)
        String jsonStr = """
            {
              "identificacao_projeto": { "numero_edital": "019/2026", "objeto_completo": "Serviços de TI" },
              "habilitacao_detalhada": [
                { "categoria": "Jurídica", "nome_documento": "Contrato Social", "descricao_exigencia": "CNAE de consultoria", "obrigatorio": true }
              ],
              "qualificacao_tecnica_especifica": [
                { "tipo_exigencia": "Atestado", "descricao_detalhada": "Experiência em nuvem", "pagina_referencia": 12 }
              ],
              "prazos_valores_e_pagamento": { "valor_estimado_total": 1000000.0, "prazo_execucao_global_dias": 365 }
            }
            """;
        try {
            licitacao.setMasterJson(mapper.readTree(jsonStr));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar JSON de teste", e);
        }
        return licitacao;
    }
}
