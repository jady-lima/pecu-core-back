package com.pecucore.system.core.dto.analise;

public record PosicaoAnimalLoteDTO(
        int posicaoGmd,
        int totalComparados,
        Double percentilGmd,
        Double diferencaGmdMediaKgDia,
        Double percentualDaMediaGmd,
        double diferencaPesoMedioKg
) {
}
