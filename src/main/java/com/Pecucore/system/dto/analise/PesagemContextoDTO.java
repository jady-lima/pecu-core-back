package com.Pecucore.system.dto.analise;

import com.Pecucore.system.model.Pesagem;

import java.time.LocalDate;

public record PesagemContextoDTO(
        LocalDate data,
        double pesoKg,
        Double gmdKgDia
) {

    public PesagemContextoDTO(Pesagem pesagem) {
        this(
                pesagem.getData(),
                pesagem.getPesoKg(),
                pesagem.getGmdCalculado()
        );
    }
}
