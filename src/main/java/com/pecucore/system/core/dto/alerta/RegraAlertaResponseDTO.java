package com.pecucore.system.core.dto.alerta;

import com.pecucore.system.core.model.RegraAlerta;
import com.pecucore.system.core.model.TipoAlerta;

public record RegraAlertaResponseDTO(
        Long id,
        TipoAlerta tipo,
        double parametro,
        boolean ativa,
        Long propriedadeId
) {

    public RegraAlertaResponseDTO(RegraAlerta regra) {
        this(
                regra.getId(),
                regra.getTipo(),
                regra.getParametro(),
                regra.isAtiva(),
                regra.getPropriedade() != null ? regra.getPropriedade().getId() : null
        );
    }
}