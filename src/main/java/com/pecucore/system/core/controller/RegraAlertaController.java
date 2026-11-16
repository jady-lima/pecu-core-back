package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.alerta.RegraAlertaRequestDTO;
import com.pecucore.system.core.dto.alerta.RegraAlertaResponseDTO;
import com.pecucore.system.core.dto.alerta.RegraAlertaUpdateRequestDTO;
import com.pecucore.system.core.model.RegraAlerta;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.alerta.RegraAlertaService;
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

@Tag(name = "Regras de alerta", description = "Regras de alerta configuráveis por propriedade")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/regras-alerta")
public class RegraAlertaController {

    @Autowired
    private RegraAlertaService regraAlertaService;

    @GetMapping
    @Operation(summary = "Listar as regras de alerta de uma propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regras listadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem acesso à propriedade"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<List<RegraAlertaResponseDTO>> getAllRegrasAlerta(
            @RequestParam Long propriedadeId,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        List<RegraAlertaResponseDTO> regras = regraAlertaService.getAllRegrasAlerta(propriedadeId, usuarioLogado)
                .stream()
                .map(RegraAlertaResponseDTO::new)
                .toList();

        return ResponseEntity.ok(regras);
    }

    @PostMapping
    @Operation(summary = "Criar regra de alerta para uma propriedade")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Regra criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro inválido, regra fixa do sistema ou regra já existente"),
        @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<RegraAlertaResponseDTO> createRegraAlerta(
        @RequestBody @Valid RegraAlertaRequestDTO dados,
        @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        RegraAlerta regra = regraAlertaService.createRegraAlerta(dados, usuarioLogado);

        return ResponseEntity.status(HttpStatus.CREATED).body(new RegraAlertaResponseDTO(regra));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar o parâmetro ou ativar e desativar uma regra de alerta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Regra atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
        @ApiResponse(responseCode = "404", description = "Regra de alerta não encontrada")
    })
    public ResponseEntity<RegraAlertaResponseDTO> updateRegraAlerta(
        @PathVariable Long id,
        @RequestBody @Valid RegraAlertaUpdateRequestDTO dados,
        @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        RegraAlerta regra = regraAlertaService.updateRegraAlerta(id, dados, usuarioLogado);

        return ResponseEntity.ok(new RegraAlertaResponseDTO(regra));
    }
}
