package com.Pecucore.system.controller;

import com.Pecucore.system.dto.AlimentacaoLoteRequestDTO;
import com.Pecucore.system.dto.AlimentacaoRequestDTO;
import com.Pecucore.system.dto.AlimentacaoResponseDTO;
import com.Pecucore.system.dto.AlimentacaoUpdateRequestDTO;
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
            @RequestBody @Valid AlimentacaoRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Alimentacao alimentacaoCriada =
                alimentacaoService.create(dados, usuarioLogado);

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

    @PutMapping("/{id}")
    @Operation(summary = "Editar alimentação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alimentação atualizada com sucesso!"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao animal."),
            @ApiResponse(responseCode = "404", description = "Alimentação não encontrada."),
            @ApiResponse(responseCode = "409", description = "A data é anterior ao nascimento do animal.")
    })
    public ResponseEntity<AlimentacaoResponseDTO> update(
            @Parameter(description = "ID da alimentação", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid AlimentacaoUpdateRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        Alimentacao alimentacaoAtualizada = alimentacaoService.update(id, dados, usuarioLogado);

        return ResponseEntity.ok(new AlimentacaoResponseDTO(alimentacaoAtualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir alimentação")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Alimentação excluída com sucesso!"),
            @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao animal."),
            @ApiResponse(responseCode = "404", description = "Alimentação não encontrada.")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da alimentação", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        alimentacaoService.delete(id, usuarioLogado);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/animal/{brinco}")
    @Operation(summary = "Listar histórico de alimentação do animal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico encontrado com sucesso!"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado.")
    })
    public ResponseEntity<List<AlimentacaoResponseDTO>> getHistorico(
            @Parameter(description = "Brinco do animal", example = "1001")
            @PathVariable Integer brinco,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        List<Alimentacao> alimentacoes =
                alimentacaoService.getHistorico(brinco, usuarioLogado);

        List<AlimentacaoResponseDTO> dtos =
                alimentacoes.stream()
                        .map(AlimentacaoResponseDTO::new)
                        .toList();

        return ResponseEntity.ok(dtos);
    }
}