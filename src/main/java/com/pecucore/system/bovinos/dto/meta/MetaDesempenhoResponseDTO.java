package com.pecucore.system.bovinos.dto.meta;

import com.pecucore.system.bovinos.model.FinalidadeLote;
import com.pecucore.system.bovinos.model.MetaDesempenho;

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