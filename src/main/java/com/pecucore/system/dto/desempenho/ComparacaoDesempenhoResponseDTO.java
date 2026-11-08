package com.pecucore.system.dto.desempenho;

import com.pecucore.system.model.FinalidadeLote;

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