package com.Pecucore.system.dto;


import com.Pecucore.system.model.Pesagem;
import java.time.LocalDate;

public record PesagemResponseDTO(
        Long id,
        Long animalId,
        double peso,
        LocalDate dataPesagem
) {

    public PesagemResponseDTO(Pesagem pesagem) {
        this
                (
                        pesagem.getId(),
                        pesagem.getAnimal().getId(),
                        pesagem.getPeso(),
                        pesagem.getDataPesagem()

                );
    }
}

