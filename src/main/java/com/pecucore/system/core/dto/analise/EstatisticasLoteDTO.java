package com.pecucore.system.core.dto.analise;

public record EstatisticasLoteDTO(
        int totalAnimaisAtivos,
        int animaisComparados,
        Double indiceMedio,
        Double indiceMediano,
        Double indiceMinimo,
        Double indiceMaximo,
        Double indiceMeta,
        Double indiceMetaMaxima,
        double pesoMedioKg,
        Double custoMedioPorAnimal
) {
}