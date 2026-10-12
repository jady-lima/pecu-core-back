package com.Pecucore.system.dto.analise;

import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.FinalidadeLote;
import com.Pecucore.system.model.StatusAnimal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record DadosAnimalDTO(
        String sexo,
        LocalDate dataNascimento,
        Long idadeMeses,
        double pesoAtualKg,
        StatusAnimal status,
        FinalidadeLote finalidadeLote,
        boolean emCarencia
) {

    public DadosAnimalDTO(Animal animal, boolean emCarencia, LocalDate dataReferencia) {
        this(
                animal.getSexo(),
                animal.getDataNascimento(),
                animal.getDataNascimento() != null ? ChronoUnit.MONTHS.between(animal.getDataNascimento(), dataReferencia) : null,
                animal.getPesoAtual(),
                animal.getStatus(),
                animal.getLote() != null ? animal.getLote().getFinalidade() : null,
                emCarencia
        );
    }
}
