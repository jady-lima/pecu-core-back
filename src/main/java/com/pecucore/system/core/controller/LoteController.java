package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.lote.LoteMetaGmdRequestDTO;
import com.pecucore.system.core.dto.lote.LoteResponseDTO;
import com.pecucore.system.core.dto.lote.LoteRequestDTO;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.LoteService;


import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Lotes", description = "Lotes das propriedades e suas metas")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/lotes")
public class LoteController {
    @Autowired
    private LoteService loteService;

    @PostMapping
    @Operation(summary = "Cadastrar lote")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lote cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada"),
            @ApiResponse(responseCode = "409", description = "Já existe um lote com esse número na propriedade")
    })
    public ResponseEntity<LoteResponseDTO> createLote(
            @RequestBody @Valid LoteRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Lote loteCriado =
                loteService.createLote(dados, usuarioLogado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new LoteResponseDTO(loteCriado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar lote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado"),
            @ApiResponse(responseCode = "409", description = "Número já usado ou capacidade menor que os animais ativos")
    })
    public ResponseEntity<LoteResponseDTO> updateLote(
            @PathVariable Long id,
            @RequestBody @Valid LoteRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Lote loteAtualizado =
                loteService.updateLote(
                        id,
                        dados,
                        usuarioLogado
                );

        return ResponseEntity.ok(
                new LoteResponseDTO(loteAtualizado)
        );
    }

    @GetMapping
    @Operation(summary = "Listar lotes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lotes listados com sucesso")
    })
    public ResponseEntity<List<LoteResponseDTO>> getAllLotes(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        List<Lote> lotes =
                loteService.getAllLotes(usuarioLogado);

        List<LoteResponseDTO> dtos =
                lotes.stream()
                        .map(LoteResponseDTO::new)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar lote por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<LoteResponseDTO> getLoteById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Lote lote =
                loteService.getLoteById(
                        id,
                        usuarioLogado
                );

        return ResponseEntity.ok(
                new LoteResponseDTO(lote)
        );
    }

    @PutMapping("/{id}/meta-gmd")
    @Operation(summary = "Definir a meta de GMD do lote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta definida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<LoteResponseDTO> updateMetaGmd(@PathVariable Long id, @RequestBody @Valid LoteMetaGmdRequestDTO dados, @AuthenticationPrincipal Usuario usuarioLogado) {

        Lote loteAtualizado =loteService.updateMetaGmd(
                id,
                dados,
                usuarioLogado
        );

        return ResponseEntity.ok(new LoteResponseDTO(loteAtualizado));
    }

    @DeleteMapping("/{id}/meta-gmd")
    @Operation(summary = "Remover a meta de GMD do lote")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Meta removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<Void> deleteMetaGmd(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioLogado) {

        loteService.deleteMetaGmd(
                id,
                usuarioLogado
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir lote")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lote excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado"),
            @ApiResponse(responseCode = "409", description = "O lote possui animais ativos")
    })
    public ResponseEntity<Void> deleteLote(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        loteService.deleteLote(
                id,
                usuarioLogado
        );

        return ResponseEntity.noContent().build();
    }
}
