package com.Pecucore.system.dto;

import com.Pecucore.system.model.Perfil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequestDTO(
        @NotBlank(message = "O campo nome não pode ser vazio")
        @Schema(
                description = "Nome do usuário",
                example = "Maria Silva"
        )
        String nome,

        @NotBlank(message = "O campo email é obrigatório.")
        @Email(message = "O campo email deve ser um endereço de email válido.")
        @Schema(
                description = "E-mail do usuário",
                example = "maria@email.com"
        )
        String username,

        @NotBlank(message = "O campo senha é obrigatório.")
        @Schema(
                description = "Senha do usuário",
                example = "123456"
        )
        String senha,

        @NotNull(message = "O tipo de perfil do usuário é obrigatório.")
        @Schema(
                description = "Perfil de acesso do usuário",
                example = "ADMIN"
        )
        Perfil perfil
) {
}
