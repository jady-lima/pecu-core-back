package com.Pecucore.system.ovinos.model;

import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import com.pecucore.system.core.model.SeveridadeAlerta;
import com.pecucore.system.core.model.UnidadeParametro;
import java.util.Optional;

public enum TipoAlertaOvinos implements TipoAlerta {
    GMD_ABAIXO_META(SeveridadeAlerta.ALTA, UnidadeParametro.INDICE_DESEMPENHO),
    PERIODO_CARENCIA(SeveridadeAlerta.ALTA, UnidadeParametro.NENHUM);

    private final SeveridadeAlerta severidade;
    private final UnidadeParametro unidadeParametro;

    TipoAlertaOvinos(SeveridadeAlerta severidade, UnidadeParametro unidadeParametro) {
        this.severidade = severidade;
        this.unidadeParametro = unidadeParametro;
    }



    @Override
    public String getCodigo() {
        return name();
    }

    @Override
    public SeveridadeAlerta getSeveridade() {
        return severidade;
    }

    @Override
    public boolean isFinanceiro() {
        return false;
    }

    @Override
    public UnidadeParametro getUnidadeParametro() {
        return unidadeParametro;
    }

    @Override
    public Optional<Double> getParametroPadrao() {
        return Optional.empty();
    }
}
