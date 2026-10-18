package com.Pecucore.system.controller;

import com.Pecucore.system.dto.ErroResponseDTO;
import com.Pecucore.system.dto.MetaDesempenhoRequestDTO;
import com.Pecucore.system.dto.MetaDesempenhoResponseDTO;
import com.Pecucore.system.model.MetaDesempenho;
import com.Pecucore.system.service.MetaDesempenhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/metas-desempenho")
public class MetaDesempenhoController {

    @Autowired
    private MetaDesempenhoService metaDesempenhoService;

    @PostMapping
    @Operation(summary = "Cadastrar meta padrão de desempenho")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Meta cadastrada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário sem permissão"),
        @ApiResponse(responseCode = "409", description = "Já existe meta cadastrada para a fase", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<MetaDesempenhoResponseDTO> create(
            @RequestBody @Valid MetaDesempenhoRequestDTO dados
    ) {

        MetaDesempenho meta = metaDesempenhoService.create(dados);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MetaDesempenhoResponseDTO(meta));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar meta padrão de desempenho")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Meta atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário sem permissão"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Já existe meta cadastrada para a fase", content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<MetaDesempenhoResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid MetaDesempenhoRequestDTO dados
    ) {

        MetaDesempenho meta = metaDesempenhoService.update(id, dados);

        return ResponseEntity.ok(new MetaDesempenhoResponseDTO(meta));
    }

    @GetMapping
    @Operation(summary = "Listar metas padrão de desempenho")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Metas listadas com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<List<MetaDesempenhoResponseDTO>> getAll() {

        List<MetaDesempenhoResponseDTO> metas =
                metaDesempenhoService.getAll()
                        .stream()
                        .map(MetaDesempenhoResponseDTO::new)
                        .toList();

        return ResponseEntity.ok(metas);
    }
}
