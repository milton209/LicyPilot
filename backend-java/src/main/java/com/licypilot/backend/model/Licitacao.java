package com.licypilot.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "licitacoes")
public class Licitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_edital")
    private String numeroEdital;

    @Column(name = "orgao_emissor")
    private String orgaoEmissor;

    @Column(columnDefinition = "TEXT")
    private String objeto;

    @Column(name = "valor_estimado")
    private Double valorEstimado;

    @Column(name = "data_abertura")
    private LocalDateTime dataAbertura;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_processamento")
    private StatusProcessamento statusProcessamento;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "master_json", columnDefinition = "jsonb")
    private JsonNode masterJson;

    @Column(name = "arquivo_url")
    private String arquivoUrl;

    @Column(name = "observacoes_erro", columnDefinition = "TEXT")
    private String observacoesErro;

    public Licitacao() {}

    public Licitacao(UUID id, String numeroEdital, String orgaoEmissor, String objeto, Double valorEstimado, LocalDateTime dataAbertura, StatusProcessamento statusProcessamento, JsonNode masterJson, String arquivoUrl, String observacoesErro) {
        this.id = id;
        this.numeroEdital = numeroEdital;
        this.orgaoEmissor = orgaoEmissor;
        this.objeto = objeto;
        this.valorEstimado = valorEstimado;
        this.dataAbertura = dataAbertura;
        this.statusProcessamento = statusProcessamento;
        this.masterJson = masterJson;
        this.arquivoUrl = arquivoUrl;
        this.observacoesErro = observacoesErro;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNumeroEdital() { return numeroEdital; }
    public void setNumeroEdital(String numeroEdital) { this.numeroEdital = numeroEdital; }

    public String getOrgaoEmissor() { return orgaoEmissor; }
    public void setOrgaoEmissor(String orgaoEmissor) { this.orgaoEmissor = orgaoEmissor; }

    public String getObjeto() { return objeto; }
    public void setObjeto(String objeto) { this.objeto = objeto; }

    public Double getValorEstimado() { return valorEstimado; }
    public void setValorEstimado(Double valorEstimado) { this.valorEstimado = valorEstimado; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public StatusProcessamento getStatusProcessamento() { return statusProcessamento; }
    public void setStatusProcessamento(StatusProcessamento statusProcessamento) { this.statusProcessamento = statusProcessamento; }

    public JsonNode getMasterJson() { return masterJson; }
    public void setMasterJson(JsonNode masterJson) { this.masterJson = masterJson; }

    public String getArquivoUrl() { return arquivoUrl; }
    public void setArquivoUrl(String arquivoUrl) { this.arquivoUrl = arquivoUrl; }

    public String getObservacoesErro() { return observacoesErro; }
    public void setObservacoesErro(String observacoesErro) { this.observacoesErro = observacoesErro; }

    public static LicitacaoBuilder builder() {
        return new LicitacaoBuilder();
    }

    public static class LicitacaoBuilder {
        private UUID id;
        private String numeroEdital;
        private String orgaoEmissor;
        private String objeto;
        private Double valorEstimado;
        private LocalDateTime dataAbertura;
        private StatusProcessamento statusProcessamento;
        private JsonNode masterJson;
        private String arquivoUrl;
        private String observacoesErro;

        public LicitacaoBuilder id(UUID id) { this.id = id; return this; }
        public LicitacaoBuilder numeroEdital(String numeroEdital) { this.numeroEdital = numeroEdital; return this; }
        public LicitacaoBuilder orgaoEmissor(String orgaoEmissor) { this.orgaoEmissor = orgaoEmissor; return this; }
        public LicitacaoBuilder objeto(String objeto) { this.objeto = objeto; return this; }
        public LicitacaoBuilder valorEstimado(Double valorEstimado) { this.valorEstimado = valorEstimado; return this; }
        public LicitacaoBuilder dataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; return this; }
        public LicitacaoBuilder statusProcessamento(StatusProcessamento statusProcessamento) { this.statusProcessamento = statusProcessamento; return this; }
        public LicitacaoBuilder masterJson(JsonNode masterJson) { this.masterJson = masterJson; return this; }
        public LicitacaoBuilder arquivoUrl(String arquivoUrl) { this.arquivoUrl = arquivoUrl; return this; }
        public LicitacaoBuilder observacoesErro(String observacoesErro) { this.observacoesErro = observacoesErro; return this; }

        public Licitacao build() {
            return new Licitacao(id, numeroEdital, orgaoEmissor, objeto, valorEstimado, dataAbertura, statusProcessamento, masterJson, arquivoUrl, observacoesErro);
        }
    }
}
