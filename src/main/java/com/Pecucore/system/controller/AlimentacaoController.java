package com.Pecucore.system.controller;

import com.Pecucore.system.dto.AlimentacaoLoteRequestDTO;
import com.Pecucore.system.dto.AlimentacaoRequestDTO;
import com.Pecucore.system.dto.AlimentacaoResponseDTO;
import com.Pecucore.system.model.Alimentacao;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.AlimentacaoService;
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
@RequestMapping("/alimentacoes")
public class AlimentacaoController {

    @Autowired
    private AlimentacaoService alimentacaoService;

    @PostMapping
    @Operation(summary = "Registrar alimentação de um animal")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alimentação registrada com sucesso!"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado."),
            @ApiResponse(responseCode = "409", description = "Animal não está ativo, ou a data é anterior ao nascimento do animal")
    })
    public ResponseEntity<AlimentacaoResponseDTO> create(
            @RequestBody @Valid AlimentacaoRequestDTO dados
    ) {

        Alimentacao alimentacaoCriada =
                alimentacaoService.create(dados);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AlimentacaoResponseDTO(alimentacaoCriada));
    }

    @PostMapping("/lote")
    @Operation(summary = "Registrar alimentação para todos os animais ativos de um lote")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alimentações registradas com sucesso!"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado."),
            @ApiResponse(responseCode = "409", description = "O lote não possui animais ativos, ou a data é anterior ao nascimento de algum animal.")
    })
    public ResponseEntity<List<AlimentacaoResponseDTO>> createEmLote(
            @RequestBody @Valid AlimentacaoLoteRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        List<Alimentacao> alimentacoes =
                alimentacaoService.createEmLote(
                        dados,
                        usuarioLogado
                );

        List<AlimentacaoResponseDTO> dtos =
                alimentacoes.stream()
                        .map(AlimentacaoResponseDTO::new)
                        .toList();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dtos);
    }

    @GetMapping("/animal/{brinco}")
    @Operation(summary = "Listar histórico de alimentação do animal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico encontrado com sucesso!"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado.")
    })
    public ResponseEntity<List<AlimentacaoResponseDTO>> getHistorico(
            @Parameter(description = "Brinco do animal", example = "1001")
            @PathVariable Integer brinco
    ) {

        List<Alimentacao> alimentacoes =
                alimentacaoService.getHistorico(brinco);

        List<AlimentacaoResponseDTO> dtos =
                alimentacoes.stream()
                        .map(AlimentacaoResponseDTO::new)
                        .toList();

        return ResponseEntity.ok(dtos);
    }
}