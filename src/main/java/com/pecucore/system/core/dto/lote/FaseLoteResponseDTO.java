package com.pecucore.system.core.dto.lote;

import com.pecucore.system.core.extensao.agrupamento.FaseLote;

public record FaseLoteResponseDTO(String codigo, String descricao) {

    public FaseLoteResponseDTO(FaseLote fase) {
        this(fase.codigo(), fase.descricao());
    }
}
