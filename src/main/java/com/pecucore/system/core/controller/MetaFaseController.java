package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.erro.ErroResponseDTO;
import com.pecucore.system.core.dto.meta.MetaFaseRequestDTO;
import com.pecucore.system.core.dto.meta.MetaFaseResponseDTO;
import com.pecucore.system.core.model.MetaFase;
import com.pecucore.system.core.service.lote.MetaFaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Metas de desempenho", description = "Metas de desempenho por fase do lote (faixa: mínimo e máximo)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/metas-desempenho")
public class MetaFaseController {

    @Autowired
    private MetaFaseService metaFaseService;

    @PostMapping
    @Operation(summary = "Cadastrar meta de desempenho de uma fase")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meta cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, fase inexistente ou mínimo maior que o máximo", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão"),
            @ApiResponse(responseCode = "409", description = "Já existe meta cadastrada para a fase", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<MetaFaseResponseDTO> createMetaFase(@RequestBody @Valid MetaFaseRequestDTO dados) {
        MetaFase metaFase = metaFaseService.createMetaFase(dados);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MetaFaseResponseDTO(metaFase));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar meta de desempenho de uma fase")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, fase inexistente ou mínimo maior que o máximo", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Já existe meta cadastrada para a fase", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<MetaFaseResponseDTO> updateMetaFase(@PathVariable Long id, @RequestBody @Valid MetaFaseRequestDTO dados) {
        MetaFase metaFase = metaFaseService.updateMetaFase(id, dados);

        return ResponseEntity.ok(new MetaFaseResponseDTO(metaFase));
    }

    @GetMapping
    @Operation(summary = "Listar metas de desempenho por fase")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metas listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<List<MetaFaseResponseDTO>> getAllMetasFase() {
        List<MetaFaseResponseDTO> metas = metaFaseService.getAllMetasFase()
                .stream()
                .map(MetaFaseResponseDTO::new)
                .toList();

        return ResponseEntity.ok(metas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar meta de desempenho por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta encontrada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada")
    })
    public ResponseEntity<MetaFaseResponseDTO> getMetaFaseById(@PathVariable Long id) {
        return ResponseEntity.ok(new MetaFaseResponseDTO(metaFaseService.getMetaFaseById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir meta de desempenho de uma fase")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Meta excluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<Void> deleteMetaFase(@PathVariable Long id) {
        metaFaseService.deleteMetaFase(id);

        return ResponseEntity.noContent().build();
    }
}