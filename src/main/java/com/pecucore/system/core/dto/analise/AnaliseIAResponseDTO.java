package com.pecucore.system.core.dto.analise;

import com.pecucore.system.core.model.AnaliseIA;
import com.pecucore.system.core.model.TipoAnalise;

import java.time.LocalDateTime;

public record AnaliseIAResponseDTO(
        Long id,
        Long animalId,
        int brinco,
        TipoAnalise tipo,
        LocalDateTime dataGeracao,
        String modelo,
        RelatorioAnaliseDTO relatorio
) {

    public AnaliseIAResponseDTO(AnaliseIA analise, RelatorioAnaliseDTO relatorio) {
        this(
                analise.getId(),
                analise.getAnimal().getId(),
                analise.getAnimal().getBrinco(),
                analise.getTipo(),
                analise.getDataGeracao(),
                analise.getModeloUtilizado(),
                relatorio
        );
    }
}
