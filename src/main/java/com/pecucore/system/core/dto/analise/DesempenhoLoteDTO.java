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
        Double indiceMetaMaxima,
        Double percentualMediaDaMeta,
        Long animaisAbaixoDaMeta,
        Long animaisAcimaDaMeta,
        double pesoMedioKg,
        Double escoreCorporalMedio,
        int animaisComEscoreCorporal
) {
}