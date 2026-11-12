package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.alerta.RegraAlertaResponseDTO;
import com.pecucore.system.core.model.RegraAlerta;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.alerta.RegraAlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Regras de alerta", description = "Regras de alerta configuráveis por propriedade")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/regras-alerta")
public class RegraAlertaController {

    @Autowired
    private RegraAlertaService regraAlertaService;

    @PostMapping
    @Operation(summary = "Criar regra de alerta para uma propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Regra criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido ou regra fixa do sistema"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<RegraAlertaResponseDTO> criarRegra(
            @RequestParam Long propriedadeId,
            @RequestParam TipoAlerta tipo,
            @RequestParam double parametro
    ) {
        RegraAlerta regra = regraAlertaService.criarRegra(propriedadeId, tipo, parametro);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegraAlertaResponseDTO(regra));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar o parâmetro ou ativar e desativar uma regra de alerta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regra atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "404", description = "Regra de alerta não encontrada")
    })
    public ResponseEntity<RegraAlertaResponseDTO> atualizarRegra(
            @PathVariable Long id,
            @RequestParam double parametro,
            @RequestParam boolean ativa
    ) {
        RegraAlerta regra = regraAlertaService.atualizarRegra(id, parametro, ativa);

        return ResponseEntity.ok(new RegraAlertaResponseDTO(regra));
    }
}