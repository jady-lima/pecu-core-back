package com.Pecucore.system.controller;

import com.Pecucore.system.dto.LoteResponseDTO;
import com.Pecucore.system.dto.LoteRequestDTO;
import com.Pecucore.system.model.Lote;
import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.service.LoteService;


import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lotes")
public class LoteController {
    @Autowired
    private LoteService loteService;

    @PostMapping
    public ResponseEntity<LoteResponseDTO> create(@RequestBody @Valid LoteRequestDTO dados) {
        Lote loteCriado = loteService.create(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(new LoteResponseDTO(loteCriado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoteResponseDTO> updateLote(
            @PathVariable Long id, @RequestBody @Valid LoteRequestDTO dados) {
        Lote loteAtualizado = loteService.update(id, dados);
        return ResponseEntity.ok(new LoteResponseDTO(loteAtualizado)
        );
    }

    @GetMapping
    public ResponseEntity<List<LoteResponseDTO>> getAllLotes() {
        List<Lote> lotes = loteService.getAllLotes();
        List<LoteResponseDTO> dtos = lotes.stream().map(LoteResponseDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoteResponseDTO> getLoteById(@PathVariable Long id) {
        Lote lote = loteService.getLoteById(id);
        return ResponseEntity.ok(new LoteResponseDTO(lote)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLote(@PathVariable Long id) {
        loteService.deleteLote(id);
        return ResponseEntity.noContent().build();
    }
}
