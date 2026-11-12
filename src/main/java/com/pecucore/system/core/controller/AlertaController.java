package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.alerta.AlertaResponseDTO;
import com.pecucore.system.core.model.StatusAlerta;
import com.pecucore.system.core.service.alerta.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alertas", description = "Alertas gerados para os animais e seu ciclo de tratamento")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/alertas")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @GetMapping
    @Operation(
            summary = "Listar alertas",
            description = "Lista os alertas das propriedades a que o usuário tem acesso. "
                    + "O técnico-veterinário não recebe alertas financeiros."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alertas listados com sucesso")
    })
    public ResponseEntity<List<AlertaResponseDTO>> getAllAlertas() {
        List<AlertaResponseDTO> alertas = alertaService.getAllAlertas().stream()
                .map(AlertaResponseDTO::new)
                .toList();

        return ResponseEntity.ok(alertas);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar o status de um alerta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Alerta não encontrado")
    })
    public ResponseEntity<AlertaResponseDTO> updateStatusAlerta(
            @PathVariable Long id,
            @RequestParam StatusAlerta status
    ) {
        return ResponseEntity.ok(
                new AlertaResponseDTO(alertaService.updateStatusAlerta(id, status))
        );
    }
}