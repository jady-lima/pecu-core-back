package com.Pecucore.system.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequestDTO(
        @NotBlank(message = "O campo username é obrigatório.")
        @Schema(
                description = "E-mail utilizado para realizar o login",
                example = "admin@email.com"
        )
        String username,

        @NotBlank(message = "O campo senha é obrigatório.")
        @Schema(
                description = "Senha do usuário",
                example = "123456"
        )
        String senha
) {
}
