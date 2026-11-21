package com.pecucore.system.core.dto.analise;

import com.pecucore.system.core.extensao.desempenho.ResultadoMeta;

public record DesempenhoContextoDTO(
        String indicador,
        String unidade,
        Double indicePeriodo,
        Long diasAvaliados,
        Double indiceMeta,
        Double indiceMetaMaxima,
        Double percentualDaMeta,
        ResultadoMeta resultadoMeta
) {
}