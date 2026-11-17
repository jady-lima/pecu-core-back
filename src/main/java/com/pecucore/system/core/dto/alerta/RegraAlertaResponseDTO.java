package com.pecucore.system.core.dto.alerta;

import com.pecucore.system.core.model.RegraAlerta;

public record RegraAlertaResponseDTO(
        Long id,
        String tipo,
        double parametro,
        boolean ativa,
        Long propriedadeId
) {

    public RegraAlertaResponseDTO(RegraAlerta regra) {
        this(
                regra.getId(),
                regra.getTipo().getCodigo(),
                regra.getParametro(),
                regra.isAtiva(),
                regra.getPropriedade() != null ? regra.getPropriedade().getId() : null
        );
    }
}