package com.pecucore.system.core.dto.analise;

public record DesempenhoContextoDTO(
        String indicador,
        String unidade,
        Double indicePeriodo,
        Long diasAvaliados,
        Double indiceMeta,
        Double percentualDaMeta
) {
}
