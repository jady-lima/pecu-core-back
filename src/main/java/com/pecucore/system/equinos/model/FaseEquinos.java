package com.pecucore.system.equinos.model;

import java.util.Arrays;
import java.util.Optional;

public enum FaseEquinos {
    POTRO("Potro"),
    JOVEM("Jovem"),
    ADULTO("Adulto"),
    REPRODUCAO("Reprodução"),
    OBSERVACAO("Observação");

    private final String descricao;

    FaseEquinos(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Optional<FaseEquinos> deCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(fase -> fase.name().equals(codigo))
                .findFirst();
    }
}