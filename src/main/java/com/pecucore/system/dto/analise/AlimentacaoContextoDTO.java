package com.pecucore.system.dto.analise;

import com.pecucore.system.model.Alimentacao;
import com.pecucore.system.model.TipoAlimentacao;

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
