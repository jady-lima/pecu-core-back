package com.Pecucore.system.dto;

import com.Pecucore.system.dto.analise.RelatorioAnaliseDTO;
import com.Pecucore.system.model.AnaliseIA;
import com.Pecucore.system.model.TipoAnalise;

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
