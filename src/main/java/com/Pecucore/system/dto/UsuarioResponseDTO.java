package com.Pecucore.system.dto;

import com.Pecucore.system.model.Perfil;
import com.Pecucore.system.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        @Schema(description = "ID do usuário", example = "1")
        Long id,

        @Schema(description = "Nome do usuário", example = "Maria Silva")
        String nome,

        @Schema(description = "E-mail do usuário", example = "maria@email.com")
        String username,

        @Schema(description = "Perfil de acesso do usuário", example = "ADMIN")
        Perfil perfil,

        @Schema(description = "Data e hora de criação do usuário", example = "2026-09-12T16:30:00")
        LocalDateTime dataCriacao) {

    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getUsername(), usuario.getPerfil(), usuario.getDataCriacao());
    }
}
