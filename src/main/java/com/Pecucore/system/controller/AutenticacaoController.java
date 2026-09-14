package com.Pecucore.system.controller;

import com.Pecucore.system.dto.LoginRequestDTO;
import com.Pecucore.system.dto.TokenResponseDTO;
import com.Pecucore.system.dto.UsuarioResponseDTO;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.TokenService;
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
