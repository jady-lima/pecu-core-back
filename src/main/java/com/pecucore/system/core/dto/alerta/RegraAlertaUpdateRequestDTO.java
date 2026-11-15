package com.pecucore.system.core.dto.alerta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegraAlertaUpdateRequestDTO(
        @NotNull(message = "O parâmetro precisa ser informado.")
        @Positive(message = "O parâmetro deve ser maior que zero.")
        Double parametro,

        @NotNull(message = "É preciso informar se a regra está ativa.")
        Boolean ativa
) {
}
