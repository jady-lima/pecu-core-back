package com.pecucore.system.core.dto.alerta;

import com.pecucore.system.core.model.TipoAlerta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegraAlertaRequestDTO(
        @NotNull(message = "A propriedade precisa ser informada.")
        Long propriedadeId,

        @NotNull(message = "O tipo de alerta precisa ser informado.")
        TipoAlerta tipo,

        @NotNull(message = "O parâmetro precisa ser informado.")
        @Positive(message = "O parâmetro deve ser maior que zero.")
        Double parametro
) {
}
