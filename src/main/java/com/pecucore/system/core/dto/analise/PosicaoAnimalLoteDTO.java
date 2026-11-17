package com.pecucore.system.core.dto.analise;

public record PosicaoAnimalLoteDTO(
        int posicaoIndice,
        int totalComparados,
        Double percentilIndice,
        Double diferencaIndiceMedia,
        Double percentualDaMediaIndice,
        double diferencaPesoMedioKg
) {
}
