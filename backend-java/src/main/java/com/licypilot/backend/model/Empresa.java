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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "empresa_cnaes", joinColumns = @JoinColumn(name = "empresa_id"))
    @Column(name = "cnae")
    private List<String> cnaes;

    public Empresa() {}

    public Empresa(UUID id, String cnpj, String razaoSocial, Double capitalSocial, List<String> cnaes) {
        this.id = id;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.capitalSocial = capitalSocial;
        this.cnaes = cnaes;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public Double getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(Double capitalSocial) { this.capitalSocial = capitalSocial; }

    public List<String> getCnaes() { return cnaes; }
    public void setCnaes(List<String> cnaes) { this.cnaes = cnaes; }
}
