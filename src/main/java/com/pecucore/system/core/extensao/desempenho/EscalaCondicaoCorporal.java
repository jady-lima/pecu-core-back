package com.pecucore.system.core.extensao.desempenho;

import java.math.BigDecimal;

public record EscalaCondicaoCorporal(double minimo, double maximo, double passo) {

    private static final double TOLERANCIA = 1e-9;

    public EscalaCondicaoCorporal {
        if (passo <= 0 || minimo >= maximo) {
            throw new IllegalArgumentException("Escala de condição corporal inválida: mínimo deve ser menor que o máximo e o passo, positivo.");
        }
    }

    public boolean aceita(double valor) {
        if (valor < minimo || valor > maximo) {
            return false;
        }

        double passos = (valor - minimo) / passo;
        return Math.abs(passos - Math.rint(passos)) < TOLERANCIA;
    }

    public String descrever() {
        return "de " + formatar(minimo) + " a " + formatar(maximo) + ", de " + formatar(passo) + " em " + formatar(passo);
    }

    private static String formatar(double valor) {
        return BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
