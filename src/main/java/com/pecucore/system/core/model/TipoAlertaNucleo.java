package com.pecucore.system.core.model;

import com.pecucore.system.core.extensao.alerta.TipoAlerta;

import java.util.Optional;

public enum TipoAlertaNucleo implements TipoAlerta {
    QUEDA_PESO(SeveridadeAlerta.ALTA, false, UnidadeParametro.NENHUM, null),
    PESAGEM_ATRASADA(SeveridadeAlerta.MEDIA, false, UnidadeParametro.DIAS, null),
    VACINA_PROX_VENC(SeveridadeAlerta.BAIXA, false, UnidadeParametro.DIAS, 7.0),
    VACINA_VENC(SeveridadeAlerta.ALTA, false, UnidadeParametro.NENHUM, null),
    CUSTO_ACIMA(SeveridadeAlerta.ALTA, true, UnidadeParametro.REAIS, null);

    private final SeveridadeAlerta severidade;
    private final boolean financeiro;
    private final UnidadeParametro unidadeParametro;
    private final Double parametroPadrao;

    TipoAlertaNucleo(SeveridadeAlerta severidade, boolean financeiro,
                     UnidadeParametro unidadeParametro, Double parametroPadrao) {
        this.severidade = severidade;
        this.financeiro = financeiro;
        this.unidadeParametro = unidadeParametro;
        this.parametroPadrao = parametroPadrao;
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
        return financeiro;
    }

    @Override
    public UnidadeParametro getUnidadeParametro() {
        return unidadeParametro;
    }

    @Override
    public Optional<Double> getParametroPadrao() {
        return Optional.ofNullable(parametroPadrao);
    }
}
