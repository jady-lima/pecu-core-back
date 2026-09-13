package com.Pecucore.system.controller;

import com.Pecucore.system.dto.AnimalRequestDTO;
import com.Pecucore.system.dto.AnimalResponseDTO;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.service.AnimalService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/animais")
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @PostMapping
    public ResponseEntity<AnimalResponseDTO> create(
            @RequestBody @Valid AnimalRequestDTO dados) {

        Animal animalCriado = animalService.create(dados);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AnimalResponseDTO(animalCriado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> updateAnimal(
            @PathVariable Long id,
            @RequestBody @Valid AnimalRequestDTO dados) {

        Animal animalAtualizado = animalService.update(id, dados);

        return ResponseEntity.ok(
                new AnimalResponseDTO(animalAtualizado)
        );
    }

    @GetMapping
    public ResponseEntity<List<AnimalResponseDTO>> getAllAnimais() {

        List<Animal> animais = animalService.getAllAnimais();

        List<AnimalResponseDTO> dtos = animais.stream()
                .map(AnimalResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> getAnimalById(
            @PathVariable Long id) {

        Animal animal = animalService.getAnimalById(id);

        return ResponseEntity.ok(
                new AnimalResponseDTO(animal)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnimal(
            @PathVariable Long id) {

        animalService.deleteAnimal(id);

        return ResponseEntity.noContent().build();
    }
}