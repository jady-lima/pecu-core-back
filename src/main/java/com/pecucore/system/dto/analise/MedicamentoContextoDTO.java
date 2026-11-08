package com.pecucore.system.dto.analise;

import com.pecucore.system.model.Medicamento;

import java.time.LocalDate;

public record MedicamentoContextoDTO(
        LocalDate dataAplicacao,
        String nome,
        String motivo,
        double dosagem,
        LocalDate dataFimCarencia
) {

    public MedicamentoContextoDTO(Medicamento medicamento) {
        this(
                medicamento.getData(),
                medicamento.getNome(),
                medicamento.getMotivo(),
                medicamento.getDosagem(),
                medicamento.getDataFimCarencia()
        );
    }
}
