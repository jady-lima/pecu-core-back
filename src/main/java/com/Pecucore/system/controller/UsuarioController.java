package com.Pecucore.system.controller;

import com.Pecucore.system.dto.UsuarioRequestDTO;
import com.Pecucore.system.dto.UsuarioResponseDTO;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.UsuarioService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    @Operation(
            summary = "Criar usuário",
            description = "Cria um novo usuário no sistema."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    public ResponseEntity<UsuarioResponseDTO> createUsuario(@RequestBody @Valid UsuarioRequestDTO dados) {
        Usuario usuarioCriado = usuarioService.createUsuario(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponseDTO(usuarioCriado));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza um usuário existente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UsuarioResponseDTO> updateUsuario(@Parameter(description = "ID do usuário que será atualizado", example = "1")@PathVariable Long id, @RequestBody @Valid UsuarioRequestDTO dados) {
        Usuario usuarioAtualizado = usuarioService.updateUsuario(id, dados);
        return ResponseEntity.ok(new UsuarioResponseDTO(usuarioAtualizado));
    }

    @GetMapping
    @Operation(
            summary = "Listar usuários",
            description = "Retorna a lista de todos os usuários existentes"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuários encontrados com sucesso")
    })
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        List<UsuarioResponseDTO> dtos = usuarios.stream().map(UsuarioResponseDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário por id",
            description = "Busca um usuário existente por id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@Parameter(description = "ID do usuário", example = "1") @PathVariable Long id) {
        Usuario usuario = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(new UsuarioResponseDTO(usuario));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar usuário por ID",
            description = "Deleta um usuário existente pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> deleteUsuario(@Parameter(description = "ID do usuário que será deletado", example = "1")@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
