package com.pecucore.system.core.service.desempenho;

import com.pecucore.system.core.model.Animal;

public record ResultadoComparacaoDesempenho(
        Animal animal,
        String indicador,
        String unidade,
        double indiceAtual,
        double indiceEsperado,
        double diferenca,
        double percentualDaMeta,
        double percentualAbaixoDaMeta,
        boolean abaixoDaMeta
) {
}
