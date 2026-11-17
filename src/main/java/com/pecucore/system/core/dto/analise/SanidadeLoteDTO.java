package com.pecucore.system.core.dto.analise;

import java.util.Map;

public record SanidadeLoteDTO(
        long animaisEmCarencia,
        Map<String, Long> alertasAtivosPorTipo
) {
}
