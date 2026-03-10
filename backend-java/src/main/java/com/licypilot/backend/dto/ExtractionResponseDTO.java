package com.licypilot.backend.dto;

import java.util.List;

public record ExtractionResponseDTO(
    String filename,
    List<SectionSegmentDTO> segments,
    boolean is_ocr
) {
    public record SectionSegmentDTO(
        int pagina_inicio,
        int pagina_fim,
        String texto_limpo
    ) {}
}
