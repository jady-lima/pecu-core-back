package com.pecucore.system.bovinos.agrupamento;

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