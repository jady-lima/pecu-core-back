package com.pecucore.system.core.dto.analise;

import java.util.Map;

public record DadosLoteDTO(
        String finalidade,
        int capacidade,
        int totalAnimaisAtivos,
        Double idadeMediaMeses,
        Map<String, Long> animaisPorSexo,
        long animaisForaDaFase
) {
}
