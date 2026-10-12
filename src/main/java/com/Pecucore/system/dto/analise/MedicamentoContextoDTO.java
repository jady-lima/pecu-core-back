package com.Pecucore.system.dto.analise;

import com.Pecucore.system.model.Medicamento;

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
