package com.pecucore.system.core.dto.animal;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;

import java.time.LocalDate;

public record AnimalResponseDTO(
        Long id,
        int brinco,
        LocalDate dataNascimento,
        double pesoAtual,
        String sexo,
        Long loteId, StatusAnimal status
) {

    public AnimalResponseDTO(Animal animal) {
        this
            (
                animal.getId(),
                animal.getBrinco(),
                animal.getDataNascimento(),
                animal.getPesoAtual(),
                animal.getSexo(),
                animal.getLote().getId(),
                animal.getStatus()
    );
    }
}