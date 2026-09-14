package com.Pecucore.system.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.UsuarioRequestDTO;
import com.Pecucore.system.dto.UsuarioUpdateRequestDTO;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = (Usuario) usuarioRepository.findByUsername(username);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }

        return usuario;
    }

    public Usuario findByUsername(String username) {
        return (Usuario) usuarioRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    public Usuario createUsuario(UsuarioRequestDTO dados) {
        if (usuarioRepository.existsByUsername(dados.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome());
        usuario.setUsername(dados.username());
        usuario.setPassword(passwordEncoder.encode(dados.senha()));
        usuario.setPerfil(dados.perfil());

        return usuarioRepository.save(usuario);
    }

    public Usuario updateUsuario(Long id, UsuarioUpdateRequestDTO dados) {
        Usuario usuarioExistente = usuarioRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (dados.nome() != null) {
            if (dados.nome().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O campo nome não pode ser vazio");
            }
            usuarioExistente.setNome(dados.nome());
        }

        if (dados.username() != null) {
            if (dados.username().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O campo email não pode ser vazio");
            }
            if (!dados.username().equals(usuarioExistente.getUsername()) && usuarioRepository.existsByUsername(dados.username())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
            }
            usuarioExistente.setUsername(dados.username());
        }

        if (dados.senha() != null) {
            if (dados.senha().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O campo senha não pode ser vazio");
            }
            usuarioExistente.setPassword(passwordEncoder.encode(dados.senha()));
        }

        if (dados.perfil() != null) {
            usuarioExistente.setPerfil(dados.perfil());
        }

        return usuarioRepository.save(usuarioExistente);
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioById(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    public void deleteUsuario(Long id) {
        Usuario usuarioExistente = usuarioRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        usuarioRepository.delete(usuarioExistente);
    }
}
