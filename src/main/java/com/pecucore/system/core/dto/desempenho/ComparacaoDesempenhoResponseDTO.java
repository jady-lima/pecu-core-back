package com.pecucore.system.core.dto.desempenho;

import com.pecucore.system.bovinos.agrupamento.FinalidadeLote;

public record ComparacaoDesempenhoResponseDTO(
        int brinco,
        FinalidadeLote fase,
        String indicador,
        String unidade,
        Double indiceAtual,
        double indiceEsperado,
        double diferenca,
        double percentualDaMeta,
        double percentualAbaixoDaMeta,
        boolean abaixoDaMeta
) {
}
