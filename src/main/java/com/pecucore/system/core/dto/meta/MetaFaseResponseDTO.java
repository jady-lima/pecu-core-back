package com.pecucore.system.core.dto.meta;

import com.pecucore.system.core.model.MetaFase;

public record MetaFaseResponseDTO(
        Long id,
        String fase,
        Double minimo,
        Double maximo
) {

    public MetaFaseResponseDTO(MetaFase metaFase) {
        this(
                metaFase.getId(),
                metaFase.getFase(),
                metaFase.getMinimo(),
                metaFase.getMaximo()
        );
    }
}