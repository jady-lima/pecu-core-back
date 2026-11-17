package com.pecucore.system.core.dto.propriedade;

import com.pecucore.system.core.model.Propriedade;

public record PropriedadeResponseDTO(Long id, String nome , String localizacao, Integer frequenciaPesagemDias, Long usuarioId) {
    public PropriedadeResponseDTO(Propriedade propriedade){
        this(
                propriedade.getId(),
                propriedade.getNome(),
                propriedade.getLocalizacao(),
                propriedade.getFrequenciaPesagemDias(),
                propriedade.getUsuario() != null ? propriedade.getUsuario().getId() : null
        );
    }
}
