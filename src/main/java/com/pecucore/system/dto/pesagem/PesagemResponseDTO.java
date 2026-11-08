package com.pecucore.system.dto.pesagem;


import com.pecucore.system.model.Pesagem;
import java.time.LocalDate;

public record PesagemResponseDTO(
        Long id,
        int brinco,
        double peso,
        LocalDate dataPesagem,
        Double gmdCalculado,
        String aviso
) {

    public PesagemResponseDTO(Pesagem pesagem) {
        this(pesagem, null);
    }

    public PesagemResponseDTO(Pesagem pesagem, String aviso) {
        this(
                pesagem.getId(),
                pesagem.getAnimal().getBrinco(),
                pesagem.getPesoKg(),
                pesagem.getData(),
                pesagem.getGmdCalculado(),
                aviso
        );
    }
}
