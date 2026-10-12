package com.Pecucore.system.controller;

import com.Pecucore.system.dto.PropriedadeRequestDTO;
import com.Pecucore.system.dto.PropriedadeResponseDTO;
import com.Pecucore.system.dto.UsuarioResponseDTO;
import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.PropriedadeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/propriedades")
public class PropriedadeController {

    @Autowired
    private PropriedadeService propriedadeService;

    @PostMapping
    public ResponseEntity<PropriedadeResponseDTO> create(
            @RequestBody @Valid PropriedadeRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Propriedade propriedadeCriada =
                propriedadeService.create(dados, usuarioLogado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new PropriedadeResponseDTO(propriedadeCriada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropriedadeResponseDTO> updatePropriedade(
            @PathVariable Long id,
            @RequestBody @Valid PropriedadeRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Propriedade propriedadeAtualizada =
                propriedadeService.update(
                        id,
                        dados,
                        usuarioLogado
                );

        return ResponseEntity.ok(
                new PropriedadeResponseDTO(propriedadeAtualizada)
        );
    }

    @GetMapping
    public ResponseEntity<List<PropriedadeResponseDTO>> getAllPropriedades(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        List<Propriedade> propriedades =
                propriedadeService.getAllPropriedades(usuarioLogado);

        List<PropriedadeResponseDTO> dtos =
                propriedades.stream()
                        .map(PropriedadeResponseDTO::new)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropriedadeResponseDTO> getPropriedadeById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Propriedade propriedade =
                propriedadeService.getPropriedadeById(
                        id,
                        usuarioLogado
                );

        return ResponseEntity.ok(
                new PropriedadeResponseDTO(propriedade)
        );
    }

    @PostMapping("/{id}/veterinarios/{veterinarioId}")
    public ResponseEntity<PropriedadeResponseDTO> vincularVeterinario(@PathVariable Long id, @PathVariable Long veterinarioId, @AuthenticationPrincipal Usuario usuarioLogado) {
        
        Propriedade propriedade = propriedadeService.vincularVeterinario(id, veterinarioId,usuarioLogado);

        return ResponseEntity.ok(new PropriedadeResponseDTO(propriedade));
    }

    @DeleteMapping("/{id}/veterinarios/{veterinarioId}")
    public ResponseEntity<Void> desvincularVeterinario(@PathVariable Long id, @PathVariable Long veterinarioId, @AuthenticationPrincipal Usuario usuarioLogado) {
        
        propriedadeService.desvincularVeterinario(id, veterinarioId, usuarioLogado);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/veterinarios")
    public ResponseEntity<List<UsuarioResponseDTO>> getVeterinarios(@PathVariable Long id,@AuthenticationPrincipal Usuario usuarioLogado) {

        List<UsuarioResponseDTO> veterinarios = propriedadeService.getVeterinarios(id, usuarioLogado)
                .stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(veterinarios);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePropriedade(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        propriedadeService.deletePropriedade(
                id,
                usuarioLogado
        );

        return ResponseEntity.noContent().build();
    }
}