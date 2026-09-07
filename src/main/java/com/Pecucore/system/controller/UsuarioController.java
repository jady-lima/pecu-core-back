package com.pecucore.system.controller;

import com.pecucore.system.dto.UsuarioRequestDTO;
import com.pecucore.system.dto.UsuarioResponseDTO;
import com.pecucore.system.model.Usuario;
import com.pecucore.system.service.UsuarioService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> createUsuario(@RequestBody @Valid UsuarioRequestDTO dados) {
        Usuario usuarioCriado = usuarioService.createUsuario(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponseDTO(usuarioCriado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> updateUsuario(@PathVariable Long id, @RequestBody @Valid UsuarioRequestDTO dados) {
        Usuario usuarioAtualizado = usuarioService.updateUsuario(id, dados);
        return ResponseEntity.ok(new UsuarioResponseDTO(usuarioAtualizado));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        List<UsuarioResponseDTO> dtos = usuarios.stream().map(UsuarioResponseDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable Long id) {
        Usuario usuario = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(new UsuarioResponseDTO(usuario));
    }
}
