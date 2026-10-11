package com.Pecucore.system.controller;

import com.Pecucore.system.dto.LoteResponseDTO;
import com.Pecucore.system.dto.LoteRequestDTO;
import com.Pecucore.system.model.Lote;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.LoteService;


import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/lotes")
public class LoteController {
    @Autowired
    private LoteService loteService;

    @PostMapping
    public ResponseEntity<LoteResponseDTO> create(
            @RequestBody @Valid LoteRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Lote loteCriado =
                loteService.create(dados, usuarioLogado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new LoteResponseDTO(loteCriado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoteResponseDTO> updateLote(
            @PathVariable Long id,
            @RequestBody @Valid LoteRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Lote loteAtualizado =
                loteService.update(
                        id,
                        dados,
                        usuarioLogado
                );

        return ResponseEntity.ok(
                new LoteResponseDTO(loteAtualizado)
        );
    }

    @GetMapping
    public ResponseEntity<List<LoteResponseDTO>> getAllLotes(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        List<Lote> lotes =
                loteService.getAllLotes(usuarioLogado);

        List<LoteResponseDTO> dtos =
                lotes.stream()
                        .map(LoteResponseDTO::new)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoteResponseDTO> getLoteById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Lote lote =
                loteService.getLoteById(
                        id,
                        usuarioLogado
                );

        return ResponseEntity.ok(
                new LoteResponseDTO(lote)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLote(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        loteService.deleteLote(
                id,
                usuarioLogado
        );

        return ResponseEntity.noContent().build();
    }
}
