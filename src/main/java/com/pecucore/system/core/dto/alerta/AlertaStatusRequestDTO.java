package com.pecucore.system.core.dto.alerta;

import com.pecucore.system.core.model.StatusAlerta;
import jakarta.validation.constraints.NotNull;

public record AlertaStatusRequestDTO(
        @NotNull(message = "O status precisa ser informado.")
        StatusAlerta status
) {
}
