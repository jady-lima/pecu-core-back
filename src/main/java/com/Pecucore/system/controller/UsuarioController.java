package com.pecucore.system.controller;

import com.pecucore.system.dto.UsuarioRequestDTO;
import com.pecucore.system.dto.UsuarioResponseDTO;
import com.pecucore.system.model.Usuario;
import com.pecucore.system.service.UsuarioService;
import jakarta.validation.Valid;
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
}
