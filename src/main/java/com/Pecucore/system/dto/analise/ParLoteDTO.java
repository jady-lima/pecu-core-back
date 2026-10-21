package com.Pecucore.system.dto.analise;

import com.Pecucore.system.model.Animal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record ParLoteDTO(
        int brinco,
        String sexo,
        Long idadeMeses,
        double pesoAtualKg,
        Double gmdPeriodoKgDia
) {

    public ParLoteDTO(Animal animal, Double gmdPeriodo, LocalDate dataReferencia) {
        this(
            animal.getBrinco(),
            animal.getSexo(),
            animal.getDataNascimento() != null ? ChronoUnit.MONTHS.between(animal.getDataNascimento(), dataReferencia) : null,
            animal.getPesoAtual(),
            gmdPeriodo
        );
    }
}
