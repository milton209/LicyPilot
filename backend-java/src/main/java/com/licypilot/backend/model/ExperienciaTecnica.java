package com.licypilot.backend.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class ExperienciaTecnica {
    private String especialidade;
    private String detalheExperiencia;

    public ExperienciaTecnica() {}
    public ExperienciaTecnica(String especialidade, String detalheExperiencia) {
        this.especialidade = especialidade;
        this.detalheExperiencia = detalheExperiencia;
    }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public String getDetalheExperiencia() { return detalheExperiencia; }
    public void setDetalheExperiencia(String detalheExperiencia) { this.detalheExperiencia = detalheExperiencia; }
}
