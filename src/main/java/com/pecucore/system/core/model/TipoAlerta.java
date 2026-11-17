package com.pecucore.system.core.model;

import java.util.Optional;

public enum TipoAlerta {
    GMD_ABAIXO_META(SeveridadeAlerta.ALTA, false, UnidadeParametro.INDICE_DESEMPENHO, null),
    QUEDA_PESO(SeveridadeAlerta.ALTA, false, UnidadeParametro.NENHUM, null),
    PESAGEM_ATRASADA(SeveridadeAlerta.MEDIA, false, UnidadeParametro.DIAS, null),
    VACINA_PROX_VENC(SeveridadeAlerta.BAIXA, false, UnidadeParametro.DIAS, 7.0),
    VACINA_VENC(SeveridadeAlerta.ALTA, false, UnidadeParametro.NENHUM, null),
    PERIODO_CARENCIA(SeveridadeAlerta.MEDIA, false, UnidadeParametro.NENHUM, null),
    CUSTO_ACIMA(SeveridadeAlerta.ALTA, true, UnidadeParametro.REAIS, null);

    private final SeveridadeAlerta severidade;
    private final boolean financeiro;
    private final UnidadeParametro unidadeParametro;
    private final Double parametroPadrao;

    TipoAlerta(SeveridadeAlerta severidade, boolean financeiro,
               UnidadeParametro unidadeParametro, Double parametroPadrao) {
        this.severidade = severidade;
        this.financeiro = financeiro;
        this.unidadeParametro = unidadeParametro;
        this.parametroPadrao = parametroPadrao;
    }

    public SeveridadeAlerta getSeveridade() {
        return severidade;
    }

    public boolean isFinanceiro() {
        return financeiro;
    }

    public UnidadeParametro getUnidadeParametro() {
        return unidadeParametro;
    }

    public boolean isConfiguravel() {
        return unidadeParametro.isConfiguravel();
    }

    public Optional<Double> getParametroPadrao() {
        return Optional.ofNullable(parametroPadrao);
    }
}
