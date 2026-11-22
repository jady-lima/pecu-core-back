package com.pecucore.system.core.dto.desempenho;

import com.pecucore.system.core.extensao.desempenho.ResultadoMeta;
import com.pecucore.system.core.service.desempenho.ResultadoComparacaoDesempenho;

public record ComparacaoDesempenhoResponseDTO(
        int brinco,
        String fase,
        String indicador,
        String unidade,
        Double indiceAtual,
        Double indiceEsperado,
        Double indiceEsperadoMaximo,
        Double diferenca,
        Double percentualDaMeta,
        Double percentualAbaixoDaMeta,
        boolean abaixoDaMeta,
        ResultadoMeta resultadoMeta
) {

    public ComparacaoDesempenhoResponseDTO(ResultadoComparacaoDesempenho resultado) {
        this(
                resultado.animal().getBrinco(),
                resultado.animal().getLote().getFinalidade(),
                resultado.indicador(),
                resultado.unidade(),
                resultado.indiceAtual(),
                resultado.indiceEsperado(),
                resultado.indiceEsperadoMaximo(),
                resultado.diferenca(),
                resultado.percentualDaMeta(),
                resultado.percentualAbaixoDaMeta(),
                resultado.abaixoDaMeta(),
                resultado.resultadoMeta()
        );
    }
}