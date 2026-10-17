package com.Pecucore.system.dto.analise;

public record EstatisticasLoteDTO(
        int totalAnimaisAtivos,
        int animaisComparados,
        Double gmdMedioKgDia,
        Double gmdMedianoKgDia,
        Double gmdMinimoKgDia,
        Double gmdMaximoKgDia,
        Double gmdMetaKgDia,
        double pesoMedioKg,
        Double custoMedioPorAnimal
) {
}
