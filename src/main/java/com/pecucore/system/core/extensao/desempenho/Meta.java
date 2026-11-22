package com.pecucore.system.core.extensao.desempenho;

/**
 * Meta de desempenho como faixa: mínimo, máximo ou os dois.
 * - Bovinos de corte: só mínimo ("ganhar pelo menos X kg/dia").
 * - Manutenção (ex.: equinos adultos): faixa, que pode ter mínimo negativo.
 * Comparar um valor com a faixa é igual para qualquer espécie; por isso fica no núcleo.
 */
public record Meta(Double minimo, Double maximo) {

    public Meta {
        if (minimo == null && maximo == null) {
            throw new IllegalArgumentException("A meta precisa de mínimo, máximo ou os dois.");
        }
        if (minimo != null && maximo != null && minimo > maximo) {
            throw new IllegalArgumentException("O mínimo da meta não pode ser maior que o máximo.");
        }
    }

    public static Meta minima(double minimo) {
        return new Meta(minimo, null);
    }

    public static Meta faixa(double minimo, double maximo) {
        return new Meta(minimo, maximo);
    }

    public ResultadoMeta avaliar(double valor) {
        if (minimo != null && valor < minimo) {
            return ResultadoMeta.ABAIXO;
        }
        if (maximo != null && valor > maximo) {
            return ResultadoMeta.ACIMA;
        }
        return ResultadoMeta.DENTRO;
    }
}