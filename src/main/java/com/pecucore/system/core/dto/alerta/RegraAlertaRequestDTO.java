package com.pecucore.system.core.dto.alerta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegraAlertaRequestDTO(
        @NotNull(message = "A propriedade precisa ser informada.")
        Long propriedadeId,

        @NotBlank(message = "O tipo de alerta precisa ser informado.")
        String tipo,

        @NotNull(message = "O parâmetro precisa ser informado.")
        @Positive(message = "O parâmetro deve ser maior que zero.")
        Double parametro
) {
}
