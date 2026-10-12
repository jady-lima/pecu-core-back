package com.Pecucore.system.dto;

import com.Pecucore.system.model.FinalidadeLote;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MetaDesempenhoRequestDTO(

        @NotNull(message = "A fase precisa ser informada.")
        FinalidadeLote fase,

        @NotNull(message = "O GMD esperado precisa ser informado.")
        @Positive(message = "O GMD esperado deve ser maior que zero.")
        Double gmdEsperado
) {
}