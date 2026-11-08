package com.pecucore.system.dto.vacinacao;

import com.pecucore.system.model.Vacinacao;

import java.time.LocalDate;

public record VacinacaoResponseDTO(
        Long id,
        int brinco,
        Long vacinaId,
        String nomeVacina,
        LocalDate data,
        String loteVacina,
        double doseAplicada,
        LocalDate dataProximaDose,
        LocalDate dataFimCarencia,
        String observacoes,
        Long aplicadorId,
        String aplicador,
        String registradoPor,
        Double valor
) {

    public VacinacaoResponseDTO(Vacinacao vacinacao) {
        this(
            vacinacao.getId(),
            vacinacao.getAnimal().getBrinco(),
            vacinacao.getVacina().getId(),
            vacinacao.getVacina().getNome(),
            vacinacao.getData(),
            vacinacao.getLoteVacina(),
            vacinacao.getDoseAplicada(),
            vacinacao.getDataProximaDose(),
            vacinacao.getDataFimCarencia(),
            vacinacao.getObservacoes(),
            vacinacao.getUsuarioAplicador().getId(),
            vacinacao.getUsuarioAplicador().getNome(),
            vacinacao.getUsuarioRegistro().getNome(),
            vacinacao.getValor()
        );
    }
}
