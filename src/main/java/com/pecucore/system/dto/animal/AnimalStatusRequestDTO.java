package com.pecucore.system.dto.animal;

import com.pecucore.system.model.StatusAnimal;
import jakarta.validation.constraints.NotNull;

public record AnimalStatusRequestDTO(
        @NotNull(message = "O status precisa ser informado!!")
        StatusAnimal status
) {}
