package com.Pecucore.system.dto;

import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.StatusAnimal;

import java.time.LocalDate;

public record AnimalResponseDTO(
        Long id,
        int brinco,
        LocalDate dataNascimento,
        double pesoInicial,
        String sexo,
        Long loteId, StatusAnimal status
) {

    public AnimalResponseDTO(Animal animal) {
        this
            (
                animal.getId(),
                animal.getBrinco(),
                animal.getDataNascimento(),
                animal.getPesoInicial(),
                animal.getSexo(),
                animal.getLote().getId(),
                animal.getStatus()
    );
    }
}