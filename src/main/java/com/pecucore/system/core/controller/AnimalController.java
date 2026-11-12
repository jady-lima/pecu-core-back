package com.pecucore.system.core.controller;



import com.pecucore.system.core.dto.animal.AnimalRequestDTO;
import com.pecucore.system.core.dto.animal.AnimalResponseDTO;
import com.pecucore.system.core.dto.animal.AnimalStatusRequestDTO;
import com.pecucore.system.core.dto.animal.FichaAnimalResponseDTO;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.AnimalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Animais", description = "Cadastro, status e ficha dos animais")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/animais")
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @PostMapping
    @Operation(summary = "Cadastrar animal")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Animal cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado"),
            @ApiResponse(responseCode = "409", description = "Brinco já cadastrado ou lote sem capacidade")
    })
    public ResponseEntity<AnimalResponseDTO> createAnimal(
            @RequestBody @Valid AnimalRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Animal animalCriado =
                animalService.createAnimal(dados, usuarioLogado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AnimalResponseDTO(animalCriado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar animal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Animal atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Animal ou lote não encontrado"),
            @ApiResponse(responseCode = "409", description = "Brinco já usado por outro animal ou alteração não permitida")
    })
    public ResponseEntity<AnimalResponseDTO> updateAnimal(
            @PathVariable Long id,
            @RequestBody @Valid AnimalRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Animal animalAtualizado =
                animalService.updateAnimal(id, dados, usuarioLogado);

        return ResponseEntity.ok(
                new AnimalResponseDTO(animalAtualizado)
        );
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Alterar o status do animal (venda, abate, óbito)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
            @ApiResponse(responseCode = "409", description = "Mudança de status não permitida ou animal em carência")
    })
    public ResponseEntity<AnimalResponseDTO> updateStatusAnimal(
            @PathVariable Long id,
            @RequestBody @Valid AnimalStatusRequestDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        Animal animalAtualizado =
                animalService.updateStatusAnimal(id, dados, usuarioLogado);

        return ResponseEntity.ok(
                new AnimalResponseDTO(animalAtualizado)
        );
    }

    @GetMapping
    @Operation(summary = "Listar animais")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Animais listados com sucesso")
    })
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
    @Operation(summary = "Buscar a ficha completa do animal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
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
    @Operation(summary = "Buscar animal por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Animal encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
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