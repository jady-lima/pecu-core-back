package com.Pecucore.system.dto;

import com.Pecucore.system.model.Perfil;
import jakarta.validation.constraints.Email;

public record UsuarioUpdateRequestDTO(
        String nome,

        @Email(message = "O campo email deve ser um endereço de email válido.")
        String username,

        String senha,

        Perfil perfil
) {
}
