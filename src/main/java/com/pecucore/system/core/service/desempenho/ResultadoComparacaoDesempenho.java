package com.pecucore.system.core.service.desempenho;

import com.pecucore.system.core.extensao.desempenho.ResultadoMeta;
import com.pecucore.system.core.model.Animal;

/**
 * indiceEsperado = mínimo da meta; indiceEsperadoMaximo = máximo (cada um nulo quando a meta não o tem).
 * diferenca e percentuais são calculados sobre o mínimo; os percentuais ficam nulos quando o mínimo não é positivo.
 */
public record ResultadoComparacaoDesempenho(
        Animal animal,
        String indicador,
        String unidade,
        double indiceAtual,
        Double indiceEsperado,
        Double indiceEsperadoMaximo,
        Double diferenca,
        Double percentualDaMeta,
        Double percentualAbaixoDaMeta,
        boolean abaixoDaMeta,
        ResultadoMeta resultadoMeta
) {
}