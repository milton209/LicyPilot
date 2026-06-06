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

    @Column(name = "titulo")
    private String titulo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "master_json", columnDefinition = "jsonb")
    private JsonNode masterJson;

    @Column(name = "arquivo_url")
    private String arquivoUrl;

    @Column(name = "arquivo_conteudo", columnDefinition = "bytea")
    private byte[] arquivoConteudo;

    @Column(name = "arquivo_hash", unique = true)
    private String arquivoHash;

    @Column(name = "observacoes_erro", columnDefinition = "TEXT")
    private String observacoesErro;

    public Licitacao() {}

    public Licitacao(UUID id, String numeroEdital, String orgaoEmissor, String objeto, Double valorEstimado, LocalDateTime dataAbertura, StatusProcessamento statusProcessamento, String titulo, JsonNode masterJson, String arquivoUrl, byte[] arquivoConteudo, String arquivoHash, String observacoesErro) {
        this.id = id;
        this.numeroEdital = numeroEdital;
        this.orgaoEmissor = orgaoEmissor;
        this.objeto = objeto;
        this.valorEstimado = valorEstimado;
        this.dataAbertura = dataAbertura;
        this.statusProcessamento = statusProcessamento;
        this.titulo = titulo;
        this.masterJson = masterJson;
        this.arquivoUrl = arquivoUrl;
        this.arquivoConteudo = arquivoConteudo;
        this.arquivoHash = arquivoHash;
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

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public JsonNode getMasterJson() { return masterJson; }
    public void setMasterJson(JsonNode masterJson) { this.masterJson = masterJson; }

    public String getArquivoUrl() { return arquivoUrl; }
    public void setArquivoUrl(String arquivoUrl) { this.arquivoUrl = arquivoUrl; }

    public byte[] getArquivoConteudo() { return arquivoConteudo; }
    public void setArquivoConteudo(byte[] arquivoConteudo) { this.arquivoConteudo = arquivoConteudo; }

    public String getArquivoHash() { return arquivoHash; }
    public void setArquivoHash(String arquivoHash) { this.arquivoHash = arquivoHash; }

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
        private String titulo;
        private JsonNode masterJson;
        private String arquivoUrl;
        private byte[] arquivoConteudo;
        private String arquivoHash;
        private String observacoesErro;

        public LicitacaoBuilder id(UUID id) { this.id = id; return this; }
        public LicitacaoBuilder numeroEdital(String numeroEdital) { this.numeroEdital = numeroEdital; return this; }
        public LicitacaoBuilder orgaoEmissor(String orgaoEmissor) { this.orgaoEmissor = orgaoEmissor; return this; }
        public LicitacaoBuilder objeto(String objeto) { this.objeto = objeto; return this; }
        public LicitacaoBuilder valorEstimado(Double valorEstimado) { this.valorEstimado = valorEstimado; return this; }
        public LicitacaoBuilder dataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; return this; }
        public LicitacaoBuilder statusProcessamento(StatusProcessamento statusProcessamento) { this.statusProcessamento = statusProcessamento; return this; }
        public LicitacaoBuilder titulo(String titulo) { this.titulo = titulo; return this; }
        public LicitacaoBuilder masterJson(JsonNode masterJson) { this.masterJson = masterJson; return this; }
        public LicitacaoBuilder arquivoUrl(String arquivoUrl) { this.arquivoUrl = arquivoUrl; return this; }
        public LicitacaoBuilder arquivoConteudo(byte[] arquivoConteudo) { this.arquivoConteudo = arquivoConteudo; return this; }
        public LicitacaoBuilder arquivoHash(String arquivoHash) { this.arquivoHash = arquivoHash; return this; }
        public LicitacaoBuilder observacoesErro(String observacoesErro) { this.observacoesErro = observacoesErro; return this; }

        public Licitacao build() {
            return new Licitacao(id, numeroEdital, orgaoEmissor, objeto, valorEstimado, dataAbertura, statusProcessamento, titulo, masterJson, arquivoUrl, arquivoConteudo, arquivoHash, observacoesErro);
        }
    }
}
