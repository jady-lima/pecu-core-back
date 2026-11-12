package com.pecucore.system.core.dto.analise;

public record DesempenhoContextoDTO(
        Double gmdPeriodoKgDia,
        Long diasAvaliados,
        Double gmdMetaKgDia,
        Double percentualDaMeta
) {
}
