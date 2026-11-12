package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.vacinacao.VacinacaoRequestDTO;
import com.pecucore.system.core.dto.vacinacao.VacinacaoResponseDTO;
import com.pecucore.system.core.dto.vacinacao.VacinacaoUpdateRequestDTO;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.model.Vacinacao;
import com.pecucore.system.core.service.VacinacaoService;

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

@Tag(name = "Vacinações", description = "Aplicações de vacinas nos animais")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/vacinacoes")
public class VacinacaoController {

    @Autowired
    private VacinacaoService vacinacaoService;

    @PostMapping
    @Operation(summary = "Registrar vacinação")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Vacinação registrada com sucesso!"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos."),
        @ApiResponse(responseCode = "404", description = "Animal ou vacina não encontrado."),
        @ApiResponse(responseCode = "409", description = "Animal não está ativo, ou a data é anterior ao nascimento do animal")
    })
    public ResponseEntity<VacinacaoResponseDTO> createVacinacao(@RequestBody @Valid VacinacaoRequestDTO dados, @AuthenticationPrincipal Usuario usuarioLogado) {
        Vacinacao vacinacaoCriada = vacinacaoService.createVacinacao(dados, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(new VacinacaoResponseDTO(vacinacaoCriada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar vacinação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vacinação atualizada com sucesso!"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos."),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao animal."),
        @ApiResponse(responseCode = "404", description = "Vacinação, vacina ou aplicador não encontrado."),
        @ApiResponse(responseCode = "409", description = "A data é anterior ao nascimento do animal.")
    })
    public ResponseEntity<VacinacaoResponseDTO> updateVacinacao(@Parameter(description = "ID da vacinação", example = "1") @PathVariable Long id, @RequestBody @Valid VacinacaoUpdateRequestDTO dados, @AuthenticationPrincipal Usuario usuarioLogado) {
        Vacinacao vacinacaoAtualizada = vacinacaoService.updateVacinacao(id, dados, usuarioLogado);
        return ResponseEntity.ok(new VacinacaoResponseDTO(vacinacaoAtualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir vacinação")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Vacinação excluída com sucesso!"),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao animal."),
        @ApiResponse(responseCode = "404", description = "Vacinação não encontrada.")
    })
    public ResponseEntity<Void> deleteVacinacao(@Parameter(description = "ID da vacinação", example = "1") @PathVariable Long id, @AuthenticationPrincipal Usuario usuarioLogado) {
        vacinacaoService.deleteVacinacao(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/animal/{brinco}")
    @Operation(summary = "Listar histórico de vacinação do animal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico encontrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    public ResponseEntity<List<VacinacaoResponseDTO>> getHistorico(@Parameter(description = "Brinco do animal", example = "1001") @PathVariable Integer brinco, @AuthenticationPrincipal Usuario usuarioLogado) {
        List<Vacinacao> vacinacoes = vacinacaoService.getHistorico(brinco, usuarioLogado);
        List<VacinacaoResponseDTO> dtos = vacinacoes.stream().map(VacinacaoResponseDTO::new).toList();
        return ResponseEntity.ok(dtos);
    }
}
