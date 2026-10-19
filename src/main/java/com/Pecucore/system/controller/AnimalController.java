package com.Pecucore.system.controller;

import com.Pecucore.system.dto.AnimalRequestDTO;
import com.Pecucore.system.dto.AnimalResponseDTO;
import com.Pecucore.system.dto.AnimalStatusRequestDTO;
import com.Pecucore.system.dto.FichaAnimalResponseDTO;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.AnimalService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/animais")
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @PostMapping
    public ResponseEntity<AnimalResponseDTO> create(
            @RequestBody @Valid AnimalRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Animal animalCriado =
                animalService.create(dados, usuarioLogado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AnimalResponseDTO(animalCriado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> updateAnimal(
            @PathVariable Long id,
            @RequestBody @Valid AnimalRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Animal animalAtualizado =
                animalService.update(id, dados, usuarioLogado);

        return ResponseEntity.ok(
                new AnimalResponseDTO(animalAtualizado)
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AnimalResponseDTO> updateStatusAnimal(
            @PathVariable Long id,
            @RequestBody @Valid AnimalStatusRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Animal animalAtualizado =
                animalService.updateStatus(id, dados, usuarioLogado);

        return ResponseEntity.ok(
                new AnimalResponseDTO(animalAtualizado)
        );
    }

    @GetMapping
    public ResponseEntity<List<AnimalResponseDTO>> getAllAnimais(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        List<Animal> animais =
                animalService.getAllAnimais(
                        usuarioLogado
                );

        List<AnimalResponseDTO> dtos =
                animais.stream()
                        .map(AnimalResponseDTO::new)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/ficha")
    public ResponseEntity<FichaAnimalResponseDTO> getFichaAnimal(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Animal animal =
                animalService.getAnimalById(id, usuarioLogado);

        return ResponseEntity.ok(
                new FichaAnimalResponseDTO(animal)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> getAnimalById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Animal animal =
                animalService.getAnimalById(id, usuarioLogado);

        return ResponseEntity.ok(
                new AnimalResponseDTO(animal)
        );
    }
}