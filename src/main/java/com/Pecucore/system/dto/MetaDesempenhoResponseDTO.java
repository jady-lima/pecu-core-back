package com.Pecucore.system.dto;

import com.Pecucore.system.model.FinalidadeLote;
import com.Pecucore.system.model.MetaDesempenho;

public record MetaDesempenhoResponseDTO(
        Long id,
        FinalidadeLote fase,
        double gmdEsperado
) {

    public MetaDesempenhoResponseDTO(MetaDesempenho meta) {
        this(
                meta.getId(),
                meta.getFase(),
                meta.getGmdEsperado()
        );
    }
}