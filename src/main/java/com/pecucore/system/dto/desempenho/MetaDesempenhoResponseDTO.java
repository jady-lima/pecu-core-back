package com.pecucore.system.dto.desempenho;

import com.pecucore.system.model.FinalidadeLote;
import com.pecucore.system.model.MetaDesempenho;

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