package com.Pecucore.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AlterarSenhaRequestDTO(
        @NotBlank(message = "O campo senha atual é obrigatório.")
        String senhaAtual,

        @NotBlank(message = "O campo nova senha é obrigatório.")
        String novaSenha
) {
}
