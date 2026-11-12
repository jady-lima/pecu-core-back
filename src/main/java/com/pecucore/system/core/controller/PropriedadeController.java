package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.propriedade.PropriedadeRequestDTO;
import com.pecucore.system.core.dto.propriedade.PropriedadeResponseDTO;
import com.pecucore.system.core.dto.usuario.UsuarioResponseDTO;
import com.pecucore.system.core.model.Propriedade;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.PropriedadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Propriedades", description = "Propriedades e veterinários vinculados")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/propriedades")
public class PropriedadeController {

    @Autowired
    private PropriedadeService propriedadeService;

    @PostMapping
    @Operation(summary = "Cadastrar propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Propriedade cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou dono da propriedade inválido")
    })
    public ResponseEntity<PropriedadeResponseDTO> create(
            @RequestBody @Valid PropriedadeRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Propriedade propriedadeCriada =
                propriedadeService.create(dados, usuarioLogado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new PropriedadeResponseDTO(propriedadeCriada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriedade atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<PropriedadeResponseDTO> updatePropriedade(
            @PathVariable Long id,
            @RequestBody @Valid PropriedadeRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Propriedade propriedadeAtualizada =
                propriedadeService.update(
                        id,
                        dados,
                        usuarioLogado
                );

        return ResponseEntity.ok(
                new PropriedadeResponseDTO(propriedadeAtualizada)
        );
    }

    @GetMapping
    @Operation(summary = "Listar propriedades")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriedades listadas com sucesso")
    })
    public ResponseEntity<List<PropriedadeResponseDTO>> getAllPropriedades(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        List<Propriedade> propriedades =
                propriedadeService.getAllPropriedades(usuarioLogado);

        List<PropriedadeResponseDTO> dtos =
                propriedades.stream()
                        .map(PropriedadeResponseDTO::new)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar propriedade por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriedade encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<PropriedadeResponseDTO> getPropriedadeById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Propriedade propriedade =
                propriedadeService.getPropriedadeById(
                        id,
                        usuarioLogado
                );

        return ResponseEntity.ok(
                new PropriedadeResponseDTO(propriedade)
        );
    }

    @PostMapping("/{id}/veterinarios/{veterinarioId}")
    @Operation(summary = "Vincular veterinário à propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veterinário vinculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "O usuário informado não tem perfil de veterinário"),
            @ApiResponse(responseCode = "404", description = "Propriedade ou usuário não encontrado")
    })
    public ResponseEntity<PropriedadeResponseDTO> vincularVeterinario(@PathVariable Long id, @PathVariable Long veterinarioId, @AuthenticationPrincipal Usuario usuarioLogado) {
        
        Propriedade propriedade = propriedadeService.vincularVeterinario(id, veterinarioId,usuarioLogado);

        return ResponseEntity.ok(new PropriedadeResponseDTO(propriedade));
    }

    @DeleteMapping("/{id}/veterinarios/{veterinarioId}")
    @Operation(summary = "Desvincular veterinário da propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Veterinário desvinculado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada ou veterinário não vinculado")
    })
    public ResponseEntity<Void> desvincularVeterinario(@PathVariable Long id, @PathVariable Long veterinarioId, @AuthenticationPrincipal Usuario usuarioLogado) {
        
        propriedadeService.desvincularVeterinario(id, veterinarioId, usuarioLogado);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/veterinarios")
    @Operation(summary = "Listar veterinários da propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veterinários listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<List<UsuarioResponseDTO>> getVeterinarios(@PathVariable Long id,@AuthenticationPrincipal Usuario usuarioLogado) {

        List<UsuarioResponseDTO> veterinarios = propriedadeService.getVeterinarios(id, usuarioLogado)
                .stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(veterinarios);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Propriedade excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<Void> deletePropriedade(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        propriedadeService.deletePropriedade(
                id,
                usuarioLogado
        );

        return ResponseEntity.noContent().build();
    }
}