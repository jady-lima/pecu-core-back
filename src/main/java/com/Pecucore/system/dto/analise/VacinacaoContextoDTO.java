package com.Pecucore.system.dto.analise;

import com.Pecucore.system.model.Vacinacao;

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
