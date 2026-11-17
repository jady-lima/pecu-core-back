package com.pecucore.system.core.model;

import java.util.List;

public enum StatusAlerta {
    ABERTO,
    EM_ANALISE,
    RESOLVIDO,
    IGNORADO;

    private static final List<StatusAlerta> ATIVOS = List.of(ABERTO, EM_ANALISE);
    private static final List<StatusAlerta> ENCERRADOS = List.of(RESOLVIDO, IGNORADO);

    public static List<StatusAlerta> ativos() {
        return ATIVOS;
    }

    public static List<StatusAlerta> encerrados() {
        return ENCERRADOS;
    }

    public boolean isEncerrado() {
        return this == RESOLVIDO || this == IGNORADO;
    }

    public boolean podeMudarPara(StatusAlerta novoStatus) {
        return novoStatus != null && novoStatus != this && !isEncerrado();
    }
}
