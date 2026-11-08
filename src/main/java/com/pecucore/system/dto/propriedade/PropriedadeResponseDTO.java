package com.pecucore.system.dto.propriedade;

import com.pecucore.system.model.Propriedade;

public record PropriedadeResponseDTO(Long id, String nome , String localizacao, Long usuarioId) {
    public PropriedadeResponseDTO(Propriedade propriedade){
        this(
                propriedade.getId(),
                propriedade.getNome(),
                propriedade.getLocalizacao(),
                propriedade.getUsuario() != null ? propriedade.getUsuario().getId() : null
        );
    }
}
