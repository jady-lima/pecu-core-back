package com.Pecucore.system.controller;

import com.Pecucore.system.dto.MedicamentoRequestDTO;
import com.Pecucore.system.dto.MedicamentoResponseDTO;
import com.Pecucore.system.model.Medicamento;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.MedicamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicamentos")
@SecurityRequirement(name = "bearerAuth")
public class MedicamentoController {

    @Autowired
    private MedicamentoService medicamentoService;

    @Operation(
            summary = "Registra um medicamento aplicado em um animal",
            description = "Registra o medicamento e calcula a data final de carência."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Medicamento registrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
            @ApiResponse(responseCode = "409", description = "Animal não está ativo ou data inválida")
    })
    @PostMapping
    public ResponseEntity<MedicamentoResponseDTO> create(
            @RequestBody @Valid MedicamentoRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        Medicamento medicamento = medicamentoService.create(dados, usuarioLogado);

        return ResponseEntity.status(201)
                .body(new MedicamentoResponseDTO(medicamento));
    }

    @Operation(
            summary = "Consulta o histórico de medicamentos de um animal"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico encontrado"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    @GetMapping("/animal/{brinco}")
    public ResponseEntity<List<MedicamentoResponseDTO>> getHistorico(
            @PathVariable int brinco,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        List<MedicamentoResponseDTO> historico = medicamentoService
                .getHistorico(brinco, usuarioLogado)
                .stream()
                .map(MedicamentoResponseDTO::new)
                .toList();

        return ResponseEntity.ok(historico);
    }
}