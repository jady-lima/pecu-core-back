package com.pecucore.system.core.model;

import java.util.List;

public enum StatusAlerta {
    ABERTO,
    EM_ANALISE,
    RESOLVIDO,
    IGNORADO;

    private static final List<StatusAlerta> ATIVOS = List.of(ABERTO, EM_ANALISE);

    public static List<StatusAlerta> ativos() {
        return ATIVOS;
    }

    public boolean isEncerrado() {
        return this == RESOLVIDO || this == IGNORADO;
    }

    public boolean podeMudarPara(StatusAlerta novoStatus) {
        return novoStatus != null && novoStatus != this && !isEncerrado();
    }
}
