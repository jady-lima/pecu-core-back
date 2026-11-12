package com.pecucore.system.core.dto.desempenho;

import com.pecucore.system.bovinos.agrupamento.FinalidadeLote;

public record ComparacaoDesempenhoResponseDTO(
        int brinco,
        FinalidadeLote fase,
        Double gmdAtual,
        double gmdEsperado,
        double diferenca,
        double percentualDaMeta,
        double percentualAbaixoDaMeta,
        boolean abaixoDaMeta
) {
}