package com.Pecucore.system.dto;

import com.Pecucore.system.model.FinalidadeLote;

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