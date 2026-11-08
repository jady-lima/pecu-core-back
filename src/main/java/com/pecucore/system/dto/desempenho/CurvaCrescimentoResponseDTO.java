package com.pecucore.system.dto.desempenho;

import com.pecucore.system.model.Pesagem;

import java.time.LocalDate;

public record CurvaCrescimentoResponseDTO(
        LocalDate data,
        double peso
) {

    public CurvaCrescimentoResponseDTO(Pesagem pesagem) {
        this(
                pesagem.getData(),
                pesagem.getPesoKg()
        );
    }
}