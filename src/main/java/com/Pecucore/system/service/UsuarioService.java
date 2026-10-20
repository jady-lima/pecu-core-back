package com.Pecucore.system.service;

import java.util.List;

import com.Pecucore.system.exception.ConflitoNegocioException;
import com.Pecucore.system.exception.RecursoNaoEncontradoException;
import com.Pecucore.system.exception.RegraNegocioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Pecucore.system.dto.AlterarSenhaRequestDTO;
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
        if (usuarioRepository.existsByUsernameIgnoreCase(dados.username())) {
            throw new ConflitoNegocioException( "E-mail já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome());
        usuario.setUsername(dados.username());
        usuario.setPassword(passwordEncoder.encode(dados.senha()));
        usuario.setPerfil(dados.perfil());

        return usuarioRepository.save(usuario);
    }

    public Usuario updateUsuario(Long id, UsuarioUpdateRequestDTO dados) {
        Usuario usuarioExistente = usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException( "Usuário não encontrado"));

        if (usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(dados.username(), id)) {
            throw new ConflitoNegocioException( "E-mail já cadastrado");
        }

        usuarioExistente.setNome(dados.nome());
        usuarioExistente.setUsername(dados.username());
        usuarioExistente.setPerfil(dados.perfil());

        if (dados.senha() != null && !dados.senha().isBlank()) {
            usuarioExistente.setPassword(passwordEncoder.encode(dados.senha()));
        }

        return usuarioRepository.save(usuarioExistente);
    }

    public void alterarSenha(Usuario usuarioLogado, AlterarSenhaRequestDTO dados) {
        Usuario usuario = getUsuarioById(usuarioLogado.getId());

        if (!passwordEncoder.matches(dados.senhaAtual(), usuario.getPassword())) {
            throw new RegraNegocioException( "Senha atual incorreta");
        }

        if (passwordEncoder.matches(dados.novaSenha(), usuario.getPassword())) {
            throw new RegraNegocioException( "A nova senha deve ser diferente da senha atual");
        }

        usuario.setPassword(passwordEncoder.encode(dados.novaSenha()));

        usuarioRepository.save(usuario);
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioById(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException( "Usuário não encontrado"));
    }

    public void deleteUsuario(Long id) {
        Usuario usuarioExistente = usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException( "Usuário não encontrado"));
        usuarioRepository.delete(usuarioExistente);
    }
}
