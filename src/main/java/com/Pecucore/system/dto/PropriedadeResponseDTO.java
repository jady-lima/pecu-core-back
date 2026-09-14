package com.Pecucore.system.dto;

import com.Pecucore.system.model.Propriedade;

public record PropriedadeResponseDTO(Long id, String nome , String localizacao) {
    public PropriedadeResponseDTO(Propriedade propriedade){
        this(propriedade.getId(), propriedade.getNome(), propriedade.getLocalizacao());
    }
}