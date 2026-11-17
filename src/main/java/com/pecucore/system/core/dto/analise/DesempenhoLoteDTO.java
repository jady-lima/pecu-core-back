package com.pecucore.system.core.dto.analise;

public record DesempenhoLoteDTO(
        String indicador,
        String unidade,
        int animaisAvaliados,
        double indiceMedio,
        double indiceMediano,
        double indiceMinimo,
        double indiceMaximo,
        Double indiceMeta,
        Double percentualMediaDaMeta,
        Long animaisAbaixoDaMeta,
        double pesoMedioKg,
        Double escoreCorporalMedio,
        int animaisComEscoreCorporal
) {
}
