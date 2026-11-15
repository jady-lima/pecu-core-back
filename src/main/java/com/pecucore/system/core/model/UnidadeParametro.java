package com.pecucore.system.core.model;

public enum UnidadeParametro {
    NENHUM,
    DIAS,
    KG_POR_DIA,
    REAIS;

    public boolean isConfiguravel() {
        return this != NENHUM;
    }
}
