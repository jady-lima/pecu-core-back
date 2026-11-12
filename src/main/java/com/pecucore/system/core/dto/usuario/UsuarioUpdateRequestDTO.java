package com.pecucore.system.core.dto.usuario;

import com.pecucore.system.core.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioUpdateRequestDTO(
        @NotBlank(message = "O campo nome não pode ser vazio")
        String nome,

        @NotBlank(message = "O campo email é obrigatório.")
        @Email(message = "O campo email deve ser um endereço de email válido.")
        String username,

        String senha,

        @NotNull(message = "O tipo de perfil do usuário é obrigatório.")
        Perfil perfil
) {
}
