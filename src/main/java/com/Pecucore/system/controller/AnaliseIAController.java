package com.Pecucore.system.controller;

import com.Pecucore.system.dto.AnaliseIAResponseDTO;
import com.Pecucore.system.model.AnaliseIA;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.AnaliseIAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analises")
@SecurityRequirement(name = "bearerAuth")
public class AnaliseIAController {

    @Autowired
    private AnaliseIAService analiseIAService;

    @Operation(summary = "Gera uma análise inteligente individual do animal com IA")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Análise gerada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao animal"),
        @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
        @ApiResponse(responseCode = "422", description = "Animal sem dados suficientes para análise"),
        @ApiResponse(responseCode = "503", description = "Serviço de IA indisponível")
    })
    @PostMapping("/animal/{animalId}")
    public ResponseEntity<AnaliseIAResponseDTO> gerarIndividual(@PathVariable Long animalId, @AuthenticationPrincipal Usuario usuarioLogado) {
        AnaliseIA analise = analiseIAService.gerarIndividual(animalId, usuarioLogado);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(analise));
    }

    @Operation(summary = "Gera uma análise comparativa do animal com os demais animais do lote")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Análise gerada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao animal"),
        @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
        @ApiResponse(responseCode = "422", description = "Animal sem lote, sem dados suficientes ou lote com poucos animais para comparação"),
        @ApiResponse(responseCode = "503", description = "Serviço de IA indisponível")
    })
    @PostMapping("/animal/{animalId}/comparativa")
    public ResponseEntity<AnaliseIAResponseDTO> gerarComparativa(@PathVariable Long animalId, @AuthenticationPrincipal Usuario usuarioLogado) {
        AnaliseIA analise = analiseIAService.gerarComparativa(animalId, usuarioLogado);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(analise));
    }

    @Operation(summary = "Consulta o histórico de análises do animal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico encontrado"),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao animal"),
        @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    @GetMapping("/animal/{animalId}")
    public ResponseEntity<List<AnaliseIAResponseDTO>> getHistoricoByAnimal(@PathVariable Long animalId, @AuthenticationPrincipal Usuario usuarioLogado) {

        List<AnaliseIAResponseDTO> analises = analiseIAService.getHistoricoByAnimal(animalId, usuarioLogado)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(analises);
    }

    @Operation(summary = "Consulta uma análise pelo id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Análise encontrada"),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso à análise"),
        @ApiResponse(responseCode = "404", description = "Análise não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AnaliseIAResponseDTO> getAnaliseById(@PathVariable Long id,@AuthenticationPrincipal Usuario usuarioLogado) {
        AnaliseIA analise = analiseIAService.getAnaliseById(id, usuarioLogado);
        return ResponseEntity.ok(toResponse(analise));
    }

    private AnaliseIAResponseDTO toResponse(AnaliseIA analise) {
        return new AnaliseIAResponseDTO(analise, analiseIAService.getRelatorio(analise));
    }
}
