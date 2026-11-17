package com.pecucore.system.core.dto.desempenho;

import com.pecucore.system.core.service.desempenho.ResultadoComparacaoDesempenho;

public record ComparacaoDesempenhoResponseDTO(
        int brinco,
        String fase,
        String indicador,
        String unidade,
        Double indiceAtual,
        double indiceEsperado,
        double diferenca,
        double percentualDaMeta,
        double percentualAbaixoDaMeta,
        boolean abaixoDaMeta
) {

    public ComparacaoDesempenhoResponseDTO(ResultadoComparacaoDesempenho resultado) {
        this(
            resultado.animal().getBrinco(),
            resultado.animal().getLote().getFinalidade(),
            resultado.indicador(),
            resultado.unidade(),
            resultado.indiceAtual(),
            resultado.indiceEsperado(),
            resultado.diferenca(),
            resultado.percentualDaMeta(),
            resultado.percentualAbaixoDaMeta(),
            resultado.abaixoDaMeta()
        );
    }
}
