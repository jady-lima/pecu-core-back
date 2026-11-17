package com.pecucore.system.core.dto.lote;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoteMetaDesempenhoRequestDTO(
        @NotNull(message = "A meta de desempenho precisa ser informada.")
        @Positive(message = "A meta de desempenho deve ser maior que zero.")
        Double metaDesempenho
) {
}
