package com.pecucore.system.core.dto.analise;

import com.pecucore.system.core.model.Alimentacao;
import com.pecucore.system.core.model.TipoAlimentacao;

import java.time.LocalDate;

public record AlimentacaoContextoDTO(
        LocalDate data,
        TipoAlimentacao tipo,
        double quantidade
) {

    public AlimentacaoContextoDTO(Alimentacao alimentacao) {
        this(
                alimentacao.getData(),
                alimentacao.getTipo(),
                alimentacao.getQuantidade()
        );
    }
}
