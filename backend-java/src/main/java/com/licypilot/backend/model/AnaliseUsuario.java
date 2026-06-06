package com.licypilot.backend.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "analises_usuario")
public class AnaliseUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "licitacao_id", nullable = false)
    private Licitacao licitacao;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_viabilidade")
    private StatusViabilidade statusViabilidade;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "diagnostico_json", columnDefinition = "jsonb")
    private JsonNode diagnosticoJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_processamento")
    private StatusProcessamento statusProcessamento = StatusProcessamento.PENDENTE;

    public AnaliseUsuario() {}

    public AnaliseUsuario(UUID id, Licitacao licitacao, Empresa empresa, StatusViabilidade statusViabilidade, JsonNode diagnosticoJson) {
        this.id = id;
        this.licitacao = licitacao;
        this.empresa = empresa;
        this.statusViabilidade = statusViabilidade;
        this.diagnosticoJson = diagnosticoJson;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Licitacao getLicitacao() { return licitacao; }
    public void setLicitacao(Licitacao licitacao) { this.licitacao = licitacao; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public StatusViabilidade getStatusViabilidade() { return statusViabilidade; }
    public void setStatusViabilidade(StatusViabilidade statusViabilidade) { this.statusViabilidade = statusViabilidade; }

    public JsonNode getDiagnosticoJson() { return diagnosticoJson; }
    public void setDiagnosticoJson(JsonNode diagnosticoJson) { this.diagnosticoJson = diagnosticoJson; }

    public StatusProcessamento getStatusProcessamento() { return statusProcessamento; }
    public void setStatusProcessamento(StatusProcessamento statusProcessamento) { this.statusProcessamento = statusProcessamento; }
}
