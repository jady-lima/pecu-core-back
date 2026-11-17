package com.pecucore.system.core.dto.alerta;

import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import com.pecucore.system.core.model.SeveridadeAlerta;

public record TipoAlertaResponseDTO(
        String codigo,
        SeveridadeAlerta severidade,
        boolean financeiro,
        boolean configuravel,
        String regraParametro,
        Double parametroPadrao
) {

    public TipoAlertaResponseDTO(TipoAlerta tipo) {
        this(
                tipo.getCodigo(),
                tipo.getSeveridade(),
                tipo.isFinanceiro(),
                tipo.isConfiguravel(),
                tipo.getUnidadeParametro().getRegraValor(),
                tipo.getParametroPadrao().orElse(null)
        );
    }
}
