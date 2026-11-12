package com.pecucore.system.core.dto.analise;

import com.pecucore.system.core.model.Vacinacao;

import java.time.LocalDate;

public record VacinacaoContextoDTO(
        LocalDate dataAplicacao,
        String vacina,
        double doseAplicada,
        LocalDate dataProximaDose,
        LocalDate dataFimCarencia
) {

    public VacinacaoContextoDTO(Vacinacao vacinacao) {
        this(
                vacinacao.getData(),
                vacinacao.getVacina().getNome(),
                vacinacao.getDoseAplicada(),
                vacinacao.getDataProximaDose(),
                vacinacao.getDataFimCarencia()
        );
    }
}
