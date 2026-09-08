package com.pecucore.system.controller;

import com.pecucore.system.dto.LoginRequestDTO;
import com.pecucore.system.dto.TokenResponseDTO;
import com.pecucore.system.dto.UsuarioResponseDTO;
import com.pecucore.system.model.Usuario;
import com.pecucore.system.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO dadosLogin) {
        var authToken = new UsernamePasswordAuthenticationToken(dadosLogin.username(), dadosLogin.senha());
        var authentication = authenticationManager.authenticate(authToken);
        var token = tokenService.generateToken((Usuario) authentication.getPrincipal());
        return ResponseEntity.ok(new TokenResponseDTO(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(new UsuarioResponseDTO(usuario));
    }
}
