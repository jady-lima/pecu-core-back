package com.Pecucore.system.controller;

import com.Pecucore.system.dto.PropriedadeRequestDTO;
import com.Pecucore.system.dto.PropriedadeResponseDTO;
import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.service.PropriedadeService;


import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/propriedades")
public class PropriedadeController {

    @Autowired
    private PropriedadeService propriedadeService;

    @PostMapping
    public ResponseEntity<PropriedadeResponseDTO> create(@RequestBody @Valid PropriedadeRequestDTO dados) {
        Propriedade propriedadeCriada = propriedadeService.create(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PropriedadeResponseDTO(propriedadeCriada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropriedadeResponseDTO> updatePropriedade(
            @PathVariable Long id, @RequestBody @Valid PropriedadeRequestDTO dados) {
        Propriedade propriedadeAtualizada = propriedadeService.update(id, dados);
        return ResponseEntity.ok(new PropriedadeResponseDTO(propriedadeAtualizada)
        );
    }

    @GetMapping
    public ResponseEntity<List<PropriedadeResponseDTO>> getAllPropriedades() {
        List<Propriedade> propriedades = propriedadeService.getAllPropriedades();
        List<PropriedadeResponseDTO> dtos = propriedades.stream().map(PropriedadeResponseDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropriedadeResponseDTO> getPropriedadeById(@PathVariable Long id) {
        Propriedade propriedade = propriedadeService.getPropriedadeById(id);
        return ResponseEntity.ok(new PropriedadeResponseDTO(propriedade)
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePropriedade(@PathVariable Long id) {
        propriedadeService.deletePropriedade(id);
        return ResponseEntity.noContent().build();
    }
}



