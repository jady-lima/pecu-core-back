package com.pecucore.system.dto;

import com.pecucore.system.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequestDTO(
        @NotBlank(message = "O campo nome não pode ser vazio")
        String nome,

        @NotBlank(message = "O campo email é obrigatório.")
        @Email(message = "O campo email deve ser um endereço de email válido.")
        String username,

        @NotBlank(message = "O campo senha é obrigatório.")
        String senha,

        @NotNull(message = "O tipo de perfil do usuário é obrigatório.")
        Perfil perfil
) {
}
