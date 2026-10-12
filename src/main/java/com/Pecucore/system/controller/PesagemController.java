package com.Pecucore.system.controller;


import com.Pecucore.system.dto.PesagemRequestDTO;
import com.Pecucore.system.dto.PesagemResponseDTO;
import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.PesagemService;
import com.Pecucore.system.service.ResultadoPesagem;
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
