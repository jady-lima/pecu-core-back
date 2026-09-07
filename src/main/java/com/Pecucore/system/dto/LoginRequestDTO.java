package com.pecucore.system.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "O campo username é obrigatório.")
        String username,

        @NotBlank(message = "O campo senha é obrigatório.")
        String senha
) {
}
