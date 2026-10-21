package com.Pecucore.system.dto.analise;

import java.time.LocalDate;
import java.util.List;

public record ContextoAnaliseComparativaDTO(
        LocalDate dataReferencia,
        ContextoAnaliseAnimalDTO animalAnalisado,
        PosicaoAnimalLoteDTO posicaoNoLote,
        EstatisticasLoteDTO estatisticasLote,
        List<ParLoteDTO> demaisAnimais
) {
}
