package com.licypilot.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record MasterJsonRecord(
    @JsonProperty("identificacao_projeto") IdentificacaoProjetoRecord identificacaoProjeto,
    @JsonProperty("prazos_valores_e_pagamento") PrazosPagamentoRecord prazosValoresPagamento,
    @JsonProperty("logistica_e_amostras") LogisticaAmostrasRecord logisticaAmostras,
    @JsonProperty("habilitacao_detalhada") List<HabilitacaoRecord> habilitacaoDetalhada,
    @JsonProperty("qualificacao_tecnica_especifica") List<QualificacaoTecnicaRecord> qualificacaoTecnicaEspecifica,
    @JsonProperty("regras_da_disputa") RegrasDisputaRecord regrasDisputa,
    @JsonProperty("analise_de_risco_e_penalidades") List<RiscoRecord> analiseRiscoPenalidades
) {
    public record IdentificacaoProjetoRecord(
        @JsonPropertyDescription("Processo licitatório, ex: 019/2026") String processo_licitatorio,
        @JsonPropertyDescription("Número do edital") String numero_edital,
        @JsonPropertyDescription("Órgão que emitiu o edital") String orgao_emissor,
        @JsonPropertyDescription("Modalidade da licitação, ex: Concorrência Eletrônica") String modalidade,
        @JsonPropertyDescription("Critério de julgamento, ex: Menor Preço") String criterio_julgamento,
        @JsonPropertyDescription("Descrição completa do objeto") String objeto_completo
    ) {}

    public record PrazosPagamentoRecord(
        @JsonPropertyDescription("Data da abertura da sessão no formato ISO8601") String data_abertura_sessao,
        @JsonPropertyDescription("Valor estimado total da licitação") Double valor_estimado_total,
        @JsonPropertyDescription("Prazo de execução em dias") Integer prazo_execucao_global_dias,
        @JsonPropertyDescription("Prazo para pagamento em dias após a nota fiscal") Integer prazo_para_pagamento_dias,
        @JsonPropertyDescription("Detalhes sobre as condições de pagamento") String condicoes_pagamento_detalhes
    ) {}

    public record LogisticaAmostrasRecord(
        @JsonPropertyDescription("Locais onde os bens/serviços devem ser entregues") String locais_de_entrega,
        @JsonPropertyDescription("Prazo de entrega em dias") Integer prazo_entrega_dias,
        @JsonPropertyDescription("Se exige amostra física (true/false)") Boolean exige_amostra_fisica,
        @JsonPropertyDescription("Detalhes sobre como deve ser a amostra") String detalhes_amostra_ou_teste
    ) {}

    public record HabilitacaoRecord(
        @JsonPropertyDescription("Categoria da habilitação (Jurídica, Fiscal, etc)") String categoria,
        @JsonPropertyDescription("Nome do documento exigido") String nome_documento,
        @JsonPropertyDescription("O que é exigido especificamente") String descricao_exigencia,
        @JsonPropertyDescription("Se é obrigatório (true/false)") Boolean obrigatorio,
        @JsonPropertyDescription("Número da página onde foi encontrado") Integer pagina_referencia,
        @JsonPropertyDescription("Trecho original do texto como prova") String trecho_original
    ) {}

    public record QualificacaoTecnicaRecord(
        @JsonPropertyDescription("Tipo de exigência técnica (Atestado, Visita, etc)") String tipo_exigencia,
        @JsonPropertyDescription("Descrição detalhada da exigência") String descricao_detalhada,
        @JsonPropertyDescription("Número da página onde foi encontrado") Integer pagina_referencia,
        @JsonPropertyDescription("Trecho original do texto como prova") String trecho_original
    ) {}

    public record RegrasDisputaRecord(
        @JsonPropertyDescription("Modo de disputa (Aberto, Fechado)") String modo_disputa,
        @JsonPropertyDescription("Vantagens para ME e EPP") String beneficio_me_epp,
        @JsonPropertyDescription("Critérios de desempate") String criterios_desempate
    ) {}

    public record RiscoRecord(
        @JsonPropertyDescription("Tipo de risco ou penalidade (Multa, Rescisão)") String tipo_risco,
        @JsonPropertyDescription("O que acontece e qual o impacto") String descricao_e_impacto,
        @JsonPropertyDescription("Número da página onde foi encontrado") Integer pagina_referencia,
        @JsonPropertyDescription("Trecho original do texto como prova") String trecho_original
    ) {}
}
