package com.Pecucore.system.dto;

import com.Pecucore.system.model.Pesagem;

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