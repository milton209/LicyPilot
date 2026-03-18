package com.licypilot.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record DiagnosticoBlocoDTO(
    @JsonProperty("atende")
    @JsonPropertyDescription("Se a empresa atende a este bloco (SIM, NAO, PARCIAL)") 
    String atende,

    @JsonProperty("pendencias")
    @JsonPropertyDescription("Lista de pendências ou riscos encontrados") 
    List<String> pendencias,

    @JsonProperty("justificativa")
    @JsonPropertyDescription("Justificativa técnica detalhada da análise") 
    String justificativa,

    @JsonProperty("trechos_originais")
    @JsonPropertyDescription("Trecho original do edital que justifica a pendência ou exigência") 
    List<String> trechos_originais,

    @JsonProperty("veredito_especialista")
    @JsonPropertyDescription("Veredito amigável e humanizado sobre este bloco (opinião do especialista)") 
    String veredito_especialista
) {}
