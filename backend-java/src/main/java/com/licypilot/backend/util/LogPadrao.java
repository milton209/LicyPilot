package com.licypilot.backend.util;

import org.slf4j.Logger;

/**
 * Padronização de logs de erro para facilitar busca e monitoramento.
 * Formato: evento=X origem=Y [contexto] resumo=Z
 */
public final class LogPadrao {

    private LogPadrao() {}

    // Eventos padronizados
    public static final String EVENTO_ERRO_IMPORTACAO = "ERRO_IMPORTACAO";
    public static final String EVENTO_ERRO_PROCESSAMENTO = "ERRO_PROCESSAMENTO";
    public static final String EVENTO_ERRO_DIAGNOSTICO_BLOCO = "ERRO_DIAGNOSTICO_BLOCO";
    public static final String EVENTO_ERRO_VEREDITO = "ERRO_VEREDITO";
    public static final String EVENTO_ERRO_DIAGNOSTICO_API = "ERRO_DIAGNOSTICO_API";
    public static final String EVENTO_ERRO_TESTE_DIAGNOSTICO = "ERRO_TESTE_DIAGNOSTICO";
    public static final String EVENTO_ERRO_MOCK_JSON = "ERRO_MOCK_JSON";
    public static final String EVENTO_ESQUEMA_NAO_APLICADO = "ESQUEMA_NAO_APLICADO";
    public static final String EVENTO_ARQUIVO_NAO_ENCONTRADO = "ARQUIVO_NAO_ENCONTRADO";
    public static final String EVENTO_ERRO_PYTHON_EXTRATOR = "ERRO_PYTHON_EXTRATOR";

    /**
     * Log de erro padronizado com stacktrace.
     * Exemplo de saída: evento=ERRO_IMPORTACAO origem=LicitacaoService.importarLicitacao arquivo=x.pdf resumo=...
     */
    public static void logErro(Logger log, String evento, String origem, String resumo, Throwable e) {
        log.error("evento={} origem={} resumo={}", evento, origem, resumo, e);
    }

    /**
     * Log de erro padronizado com contexto adicional (ex: arquivo, id).
     */
    public static void logErro(Logger log, String evento, String origem, String contextoChave, Object contextoValor, String resumo, Throwable e) {
        log.error("evento={} origem={} {}={} resumo={}", evento, origem, contextoChave, contextoValor, resumo, e);
    }

    /**
     * Log de erro padronizado sem exceção (ex: arquivo não encontrado).
     */
    public static void logErro(Logger log, String evento, String origem, String contextoChave, Object contextoValor, String resumo) {
        log.error("evento={} origem={} {}={} resumo={}", evento, origem, contextoChave, contextoValor, resumo);
    }

    /**
     * Log de erro com contexto extra em string (ex: "licitacaoId=uuid arquivo=x.pdf").
     */
    public static void logErro(Logger log, String evento, String origem, String contextoExtra, String resumo, Throwable e) {
        if (contextoExtra == null || contextoExtra.isBlank()) {
            log.error("evento={} origem={} resumo={}", evento, origem, resumo, e);
        } else {
            log.error("evento={} origem={} {} resumo={}", evento, origem, contextoExtra, resumo, e);
        }
    }
}
