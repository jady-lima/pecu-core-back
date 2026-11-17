package com.pecucore.system.core.extensao.alerta;

import com.pecucore.system.core.model.SeveridadeAlerta;
import com.pecucore.system.core.model.UnidadeParametro;

import java.util.Optional;

public interface TipoAlerta {

    String getCodigo();

    SeveridadeAlerta getSeveridade();

    boolean isFinanceiro();

    UnidadeParametro getUnidadeParametro();

    Optional<Double> getParametroPadrao();

    default boolean isConfiguravel() {
        return getUnidadeParametro().isConfiguravel();
    }
}
