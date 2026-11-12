package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.desempenho.ComparacaoDesempenhoResponseDTO;
import com.pecucore.system.core.dto.desempenho.CurvaCrescimentoResponseDTO;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.DesempenhoService;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Desempenho", description = "Curva de crescimento e comparação com a meta")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/desempenho")
public class DesempenhoController {

    @Autowired
    private DesempenhoService desempenhoService;

    @GetMapping("/animal/{brinco}/curva")
    @Operation(summary = "Buscar a curva de crescimento do animal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Curva encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    public ResponseEntity<List<CurvaCrescimentoResponseDTO>> getCurvaCrescimento(
            @PathVariable int brinco,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        List<CurvaCrescimentoResponseDTO> curva =
                desempenhoService.getCurvaCrescimento(brinco, usuarioLogado);

        return ResponseEntity.ok(curva);
    }

    @GetMapping("/animal/{brinco}/comparacao")
    @Operation(summary = "Comparar o desempenho do animal com a meta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comparação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Animal ou meta de desempenho não encontrada"),
            @ApiResponse(responseCode = "409", description = "Pesagens insuficientes para calcular o desempenho")
    })
    public ResponseEntity<ComparacaoDesempenhoResponseDTO> comparar(
            @PathVariable int brinco,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        ComparacaoDesempenhoResponseDTO resultado =
                desempenhoService.comparar(brinco, usuarioLogado);

        return ResponseEntity.ok(resultado);
    }
}