package com.Pecucore.system.controller;

import com.Pecucore.system.dto.VacinaRequestDTO;
import com.Pecucore.system.dto.VacinaResponseDTO;
import com.Pecucore.system.model.Vacina;
import com.Pecucore.system.service.VacinaService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/vacinas")
public class VacinaController {

    @Autowired
    private VacinaService vacinaService;

    @PostMapping
    @Operation(summary = "Cadastrar vacina no catálogo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Vacina cadastrada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Já existe uma vacina com este nome")
    })
    public ResponseEntity<VacinaResponseDTO> create(@RequestBody @Valid VacinaRequestDTO dados) {
        Vacina vacinaCriada = vacinaService.create(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(new VacinaResponseDTO(vacinaCriada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar vacina do catálogo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vacina atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Vacina não encontrada"),
        @ApiResponse(responseCode = "409", description = "Já existe outra vacina com este nome")
    })
    public ResponseEntity<VacinaResponseDTO> updateVacina(@PathVariable Long id, @RequestBody @Valid VacinaRequestDTO dados) {
        Vacina vacinaAtualizada = vacinaService.update(id, dados);
        return ResponseEntity.ok(new VacinaResponseDTO(vacinaAtualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir vacina do catálogo")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Vacina excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Vacina não encontrada"),
        @ApiResponse(responseCode = "409", description = "Vacina já aplicada em algum animal")
    })
    public ResponseEntity<Void> deleteVacina(@PathVariable Long id) {
        vacinaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar vacinas do catálogo")
    public ResponseEntity<List<VacinaResponseDTO>> getAllVacinas() {
        List<Vacina> vacinas = vacinaService.getAllVacinas();
        List<VacinaResponseDTO> dtos = vacinas.stream().map(VacinaResponseDTO::new).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar vacina por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vacina encontrada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Vacina não encontrada")
    })
    public ResponseEntity<VacinaResponseDTO> getVacinaById(@PathVariable Long id) {
        Vacina vacina = vacinaService.getVacinaById(id);
        return ResponseEntity.ok(new VacinaResponseDTO(vacina));
    }
}
