package com.pecucore.system.dto;

import com.pecucore.system.model.Perfil;
import com.pecucore.system.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(Long id, String nome, String username, Perfil perfil, LocalDateTime dataCriacao) {

    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getUsername(), usuario.getPerfil(), usuario.getDataCriacao());
    }
}
