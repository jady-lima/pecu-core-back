package com.pecucore.system.dto.analise;

import com.pecucore.system.model.Pesagem;

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
