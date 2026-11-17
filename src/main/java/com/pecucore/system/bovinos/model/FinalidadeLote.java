package com.pecucore.system.bovinos.model;

import java.util.Arrays;
import java.util.Optional;

public enum FinalidadeLote {
    CRIA("Cria"),
    RECRIA("Recria"),
    ENGORDA("Engorda"),
    CORTE("Corte"),
    REPRODUCAO("Reprodução"),
    QUARENTENA("Quarentena"),
    DESMAME("Desmame");

    private final String descricao;

    FinalidadeLote(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Optional<FinalidadeLote> deCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(finalidade -> finalidade.name().equals(codigo))
                .findFirst();
    }
}
