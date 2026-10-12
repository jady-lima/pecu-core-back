package com.Pecucore.system.dto;

import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.FinalidadeLote;
import com.Pecucore.system.model.StatusAnimal;

import java.time.LocalDate;

public record FichaAnimalResponseDTO(
        Long id,
        int brinco,
        LocalDate dataNascimento,
        double pesoAtual,
        String sexo,
        StatusAnimal status,

        Long loteId,
        int numeroLote,
        FinalidadeLote finalidadeLote,
        int capacidadeLote,
        LocalDate dataCriacaoLote,

        Long propriedadeId,
        String nomePropriedade,
        String localizacaoPropriedade
) {

    public FichaAnimalResponseDTO(Animal animal) {
        this(
                animal.getId(),
                animal.getBrinco(),
                animal.getDataNascimento(),
                animal.getPesoAtual(),
                animal.getSexo(),
                animal.getStatus(),

                animal.getLote().getId(),
                animal.getLote().getNumero(),
                animal.getLote().getFinalidade(),
                animal.getLote().getCapacidade(),
                animal.getLote().getDataCriacao(),

                animal.getLote().getPropriedade().getId(),
                animal.getLote().getPropriedade().getNome(),
                animal.getLote().getPropriedade().getLocalizacao()
        );
    }
}