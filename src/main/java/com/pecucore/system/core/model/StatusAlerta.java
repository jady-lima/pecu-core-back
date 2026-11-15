package com.pecucore.system.core.model;

public enum StatusAlerta {
    ABERTO,
    EM_ANALISE,
    RESOLVIDO,
    IGNORADO;

    public boolean isEncerrado() {
        return this == RESOLVIDO || this == IGNORADO;
    }

    public boolean podeMudarPara(StatusAlerta novoStatus) {
        return novoStatus != null && novoStatus != this && !isEncerrado();
    }
}
