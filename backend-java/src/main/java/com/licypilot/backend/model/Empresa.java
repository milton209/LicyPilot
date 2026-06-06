package com.licypilot.backend.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String cnpj;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(name = "capital_social")
    private Double capitalSocial;

    @Column(name = "porte")
    private String porte; // ex: ME, EPP, DEMAIS

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "empresa_cnaes", joinColumns = @JoinColumn(name = "empresa_id"))
    @Column(name = "cnae")
    private List<String> cnaes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "empresa_documentos", joinColumns = @JoinColumn(name = "empresa_id"))
    @Column(name = "documento")
    private List<String> documentosRegulares;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "empresa_experiencias", joinColumns = @JoinColumn(name = "empresa_id"))
    private List<ExperienciaTecnica> experienciasTecnicas;

    public Empresa() {}

    public Empresa(UUID id, String cnpj, String razaoSocial, Double capitalSocial, String porte, List<String> cnaes, List<String> documentosRegulares, List<ExperienciaTecnica> experienciasTecnicas) {
        this.id = id;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.capitalSocial = capitalSocial;
        this.porte = porte;
        this.cnaes = cnaes;
        this.documentosRegulares = documentosRegulares;
        this.experienciasTecnicas = experienciasTecnicas;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public Double getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(Double capitalSocial) { this.capitalSocial = capitalSocial; }

    public String getPorte() { return porte; }
    public void setPorte(String porte) { this.porte = porte; }

    public List<String> getCnaes() { return cnaes; }
    public void setCnaes(List<String> cnaes) { this.cnaes = cnaes; }

    public List<String> getDocumentosRegulares() { return documentosRegulares; }
    public void setDocumentosRegulares(List<String> documentosRegulares) { this.documentosRegulares = documentosRegulares; }

    public List<ExperienciaTecnica> getExperienciasTecnicas() { return experienciasTecnicas; }
    public void setExperienciasTecnicas(List<ExperienciaTecnica> experienciasTecnicas) { this.experienciasTecnicas = experienciasTecnicas; }
}
