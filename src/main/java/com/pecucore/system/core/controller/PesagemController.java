package com.pecucore.system.core.controller;


import com.pecucore.system.core.dto.pesagem.PesagemRequestDTO;
import com.pecucore.system.core.dto.pesagem.PesagemResponseDTO;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.pesagem.PesagemService;
import com.pecucore.system.core.service.pesagem.ResultadoPesagem;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Pesagens", description = "Registros de pesagem e cálculo de GMD")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/pesagens")
public class PesagemController {

    @Autowired
    private PesagemService pesagemService;  

    @PostMapping
    @Operation(summary = "Registrar pesagem")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pesagem registrada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos (peso não positivo, data futura ou campo ausente)"),
        @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
        @ApiResponse(responseCode = "409", description = "Animal não está ativo, ou a data da pesagem é anterior ao nascimento do animal ou à última pesagem registrada")
    })
    public ResponseEntity<PesagemResponseDTO> create(@RequestBody @Valid PesagemRequestDTO dados, @AuthenticationPrincipal Usuario usuarioLogado) {
        ResultadoPesagem resultado = pesagemService.create(dados, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PesagemResponseDTO(resultado.pesagem(), resultado.aviso()));
    }

    @GetMapping("/animal/{brinco}")
    @Operation(summary = "Listar histórico de pesagens do animal") 
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico encontrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    public ResponseEntity<List<PesagemResponseDTO>> getHistorico( @Parameter(description = "Brinco do animal", example = "1001") @PathVariable Integer brinco, @AuthenticationPrincipal Usuario usuarioLogado) {
        List<Pesagem> pesagens = pesagemService.getHistorico(brinco, usuarioLogado);
        List<PesagemResponseDTO> dtos = pesagens.stream().map(PesagemResponseDTO::new).toList();
        return ResponseEntity.ok(dtos);
    }
}
