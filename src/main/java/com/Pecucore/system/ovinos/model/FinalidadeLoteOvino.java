package com.Pecucore.system.ovinos.model;

import java.util.Arrays;
import java.util.Optional;

public enum FinalidadeLoteOvino {

        CORDEIRO("Cordeiro"),
        DESMAME("Desmame"),
        RECRIA("Recria"),
        TERMINACAO("Terminacao"),
        QUARENTENA("Quarentena"),
        REPRODUCAO("Reproducao");


        private final String descricao;

        FinalidadeLoteOvino(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }

        public static Optional<FinalidadeLoteOvino> deCodigo(String codigo) {
            return Arrays.stream(values())
                    .filter(finalidade -> finalidade.name().equals(codigo))
                    .findFirst();
        }


    }
