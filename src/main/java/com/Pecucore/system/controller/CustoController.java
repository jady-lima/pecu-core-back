package com.Pecucore.system.controller;

import com.Pecucore.system.dto.CustoRequestDTO;
import com.Pecucore.system.dto.CustoResponseDTO;
import com.Pecucore.system.model.Custo;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.CustoService;
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
@RequestMapping("/custos")
@SecurityRequirement(name = "bearerAuth")
public class CustoController {

    @Autowired
    private CustoService custoService;

    @Operation(
            summary = "Registra um custo manual"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Custo registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao animal, lote ou propriedade"),
            @ApiResponse(responseCode = "404", description = "Animal, lote ou propriedade não encontrado")
    })
    @PostMapping
    public ResponseEntity<CustoResponseDTO> create(
            @RequestBody @Valid CustoRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        Custo custo = custoService.create(dados, usuarioLogado);

        return ResponseEntity.status(201)
                .body(new CustoResponseDTO(custo));
    }

    @Operation(summary = "Edita um custo manual")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Custo atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao custo, animal, lote ou propriedade"),
        @ApiResponse(responseCode = "404", description = "Custo, animal, lote ou propriedade não encontrado"),
        @ApiResponse(responseCode = "409", description = "Custo automático não pode ser editado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustoResponseDTO> update(
        @PathVariable Long id,
        @RequestBody @Valid CustoRequestDTO dados,
        @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        Custo custo = custoService.update(id, dados, usuarioLogado);

        return ResponseEntity.ok(new CustoResponseDTO(custo));
    }

    @Operation(summary = "Exclui um custo manual")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Custo excluído com sucesso"),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao custo"),
        @ApiResponse(responseCode = "404", description = "Custo não encontrado"),
        @ApiResponse(responseCode = "409", description = "Custo automático não pode ser excluído")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable Long id,
        @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        custoService.delete(id, usuarioLogado);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca um custo por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Custo encontrado"),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao custo"),
        @ApiResponse(responseCode = "404", description = "Custo não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustoResponseDTO> getById(
        @PathVariable Long id,
        @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        Custo custo = custoService.getCustoById(id, usuarioLogado);

        return ResponseEntity.ok(new CustoResponseDTO(custo));
    }

    @Operation(
            summary = "Consulta os custos das propriedades do produtor"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Custos encontrados")
    })
    @GetMapping
    public ResponseEntity<List<CustoResponseDTO>> getAll(
            @AuthenticationPrincipal Usuario usuarioLogado) {

        List<CustoResponseDTO> custos = custoService.getAll(usuarioLogado)
                .stream()
                .map(CustoResponseDTO::new)
                .toList();

        return ResponseEntity.ok(custos);
    }
}