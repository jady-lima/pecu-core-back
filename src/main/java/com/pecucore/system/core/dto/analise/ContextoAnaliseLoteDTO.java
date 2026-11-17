package com.pecucore.system.core.dto.analise;

import java.time.LocalDate;

public record ContextoAnaliseLoteDTO(
        LocalDate dataReferencia,
        DadosLoteDTO lote,
        DesempenhoLoteDTO desempenho,
        SanidadeLoteDTO sanidade,
        Double custoMedioPorAnimal
) {
}
