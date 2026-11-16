package com.pecucore.system.core.model;

public enum UnidadeParametro {
    NENHUM(false, null),
    DIAS(true, "um número inteiro de dias maior que zero"),
    KG_POR_DIA(false, "um valor em kg/dia maior que zero"),
    REAIS(false, "um valor em reais maior que zero");

    private final boolean inteiro;
    private final String regraValor;

    UnidadeParametro(boolean inteiro, String regraValor) {
        this.inteiro = inteiro;
        this.regraValor = regraValor;
    }

    public boolean isConfiguravel() {
        return this != NENHUM;
    }

    public boolean aceita(double valor) {
        return isConfiguravel() && Double.isFinite(valor)
                                && valor > 0
                                && (!inteiro || valor == Math.floor(valor));
    }

    public String getRegraValor() {
        return regraValor;
    }
}
