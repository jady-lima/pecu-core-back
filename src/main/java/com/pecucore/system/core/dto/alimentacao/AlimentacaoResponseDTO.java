package com.pecucore.system.core.dto.alimentacao;

import com.pecucore.system.core.model.Alimentacao;
import com.pecucore.system.core.model.TipoAlimentacao;

import java.time.LocalDate;

public record AlimentacaoResponseDTO(
        Long id,
        int brinco,
        TipoAlimentacao tipo,
        double quantidade,
        LocalDate data,
        Double valor
) {

    public AlimentacaoResponseDTO(Alimentacao alimentacao) {
        this(
            alimentacao.getId(),
            alimentacao.getAnimal().getBrinco(),
            alimentacao.getTipo(),
            alimentacao.getQuantidade(),
            alimentacao.getData(),
            alimentacao.getValor()
        );
    }
}
