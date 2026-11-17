package com.pecucore.system.core.dto.analise;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;

import java.time.LocalDate;

public record DadosAnimalDTO(
        String sexo,
        LocalDate dataNascimento,
        Long idadeMeses,
        double pesoAtualKg,
        StatusAnimal status,
        String finalidadeLote,
        boolean emCarencia
) {

    public DadosAnimalDTO(Animal animal, boolean emCarencia, LocalDate dataReferencia) {
        this(
            animal.getSexo(),
            animal.getDataNascimento(),
            animal.getIdadeMeses(dataReferencia).orElse(null),
            animal.getPesoAtual(),
            animal.getStatus(),
            animal.getLote() != null ? animal.getLote().getFinalidade() : null,
            emCarencia
        );
    }
}
