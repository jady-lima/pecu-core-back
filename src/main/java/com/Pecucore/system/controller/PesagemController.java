package com.Pecucore.system.controller;


import com.Pecucore.system.dto.PesagemRequestDTO;
import com.Pecucore.system.dto.PesagemResponseDTO;
import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.service.PesagemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/pesagens")
public class PesagemController {

    @Autowired
    private PesagemService pesagemService;

    @PostMapping
    public ResponseEntity<PesagemResponseDTO> create(@RequestBody @Valid PesagemRequestDTO dados) {
        Pesagem pesagemCriada = pesagemService.create(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PesagemResponseDTO(pesagemCriada));
    }

    @GetMapping("/animal/{animalId}")
    public ResponseEntity<List<PesagemResponseDTO>> getHistorico(@PathVariable Long animalId) {
        List<Pesagem> pesagens = pesagemService.getHistorico(animalId);
        List<PesagemResponseDTO> dtos = pesagens.stream().map(PesagemResponseDTO::new).toList();
        return ResponseEntity.ok(dtos);
    }
}
