package com.pecucore.system.core.dto.vacinacao;

import com.pecucore.system.core.model.Vacina;

public record VacinaResponseDTO(
        Long id,
        String nome,
        String fabricante,
        int carenciaDias,
        Integer intervaloDoseDias
) {

    public VacinaResponseDTO(Vacina vacina) {
        this(
            vacina.getId(),
            vacina.getNome(),
            vacina.getFabricante(),
            vacina.getCarenciaDias(),
            vacina.getIntervaloDoseDias()
        );
    }
}
