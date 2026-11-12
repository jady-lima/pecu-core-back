package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.usuario.AlterarSenhaRequestDTO;
import com.pecucore.system.core.dto.erro.ErroResponseDTO;
import com.pecucore.system.core.dto.autenticacao.LoginRequestDTO;
import com.pecucore.system.core.dto.autenticacao.TokenResponseDTO;
import com.pecucore.system.core.dto.usuario.UsuarioResponseDTO;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.TokenService;
import com.pecucore.system.core.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;


@Tag(name = "Autenticação", description = "Login, logout e dados do usuário autenticado")
@RestController
@RequestMapping("/auth")

public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(summary = "Realizar login")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso", content = @Content(schema = @Schema(implementation = TokenResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuário ou senha inválidos", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO dadosLogin) {
        var authToken = new UsernamePasswordAuthenticationToken(dadosLogin.username(), dadosLogin.senha());
        var authentication = authenticationManager.authenticate(authToken);
        var token = tokenService.generateToken((Usuario) authentication.getPrincipal());
        return ResponseEntity.ok(new TokenResponseDTO(token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Realizar logout", description = "Encerra a sessão do usuário autenticado. O cliente deve descartar o token JWT armazenado")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Buscar usuário autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário autenticado encontrado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<UsuarioResponseDTO> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(new UsuarioResponseDTO(usuario));
    }

    @PutMapping("/me/senha")
    @Operation(summary = "Alterar a própria senha")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, senha atual incorreta ou nova senha igual à atual", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<Void> alterarSenha(@RequestBody @Valid AlterarSenhaRequestDTO dados, @AuthenticationPrincipal Usuario usuario) {
        usuarioService.alterarSenha(usuario, dados);
        return ResponseEntity.noContent().build();
    }
}
