package com.Pecucore.system.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoteMetaGmdRequestDTO(
        @NotNull(message = "O GMD esperado precisa ser informado.")
        @Positive(message = "O GMD esperado deve ser maior que zero.")
        Double gmdEsperado
) {
}
