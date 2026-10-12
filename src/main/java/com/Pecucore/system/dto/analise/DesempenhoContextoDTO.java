package com.Pecucore.system.dto.analise;

public record DesempenhoContextoDTO(
        Double gmdPeriodoKgDia,
        Long diasAvaliados,
        Double gmdMetaKgDia,
        Double percentualDaMeta
) {
}
