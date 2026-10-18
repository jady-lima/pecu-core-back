package com.Pecucore.system.dto.analise;

public record PosicaoAnimalLoteDTO(
        int posicaoGmd,
        int totalComparados,
        Double percentilGmd,
        Double diferencaGmdMediaKgDia,
        Double percentualDaMediaGmd,
        double diferencaPesoMedioKg
) {
}
