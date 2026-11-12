package com.pecucore.system.core.dto.animal;

import com.pecucore.system.core.model.StatusAnimal;
import jakarta.validation.constraints.NotNull;

public record AnimalStatusRequestDTO(
        @NotNull(message = "O status precisa ser informado!!")
        StatusAnimal status
) {}
