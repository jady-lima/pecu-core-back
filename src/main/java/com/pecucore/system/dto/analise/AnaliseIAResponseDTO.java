package com.pecucore.system.dto.analise;

import com.pecucore.system.model.AnaliseIA;
import com.pecucore.system.model.TipoAnalise;

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
