package com.pecucore.system.core.dto.analise;

import com.pecucore.system.core.model.Animal;

import java.time.LocalDate;

public record ParLoteDTO(
        int brinco,
        String sexo,
        Long idadeMeses,
        double pesoAtualKg,
        Double indicePeriodo
) {

    public ParLoteDTO(Animal animal, Double indicePeriodo, LocalDate dataReferencia) {
        this(
            animal.getBrinco(),
            animal.getSexo(),
            animal.getIdadeMeses(dataReferencia).orElse(null),
            animal.getPesoAtual(),
            indicePeriodo
        );
    }
}
