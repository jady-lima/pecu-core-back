package com.pecucore.system.core.dto.analise;

import com.pecucore.system.core.model.AnaliseIA;
import com.pecucore.system.core.model.TipoAnalise;

import java.time.LocalDateTime;

public record AnaliseIAResponseDTO(
        Long id,
        Long animalId,
        Integer brinco,
        Long loteId,
        Integer numeroLote,
        TipoAnalise tipo,
        LocalDateTime dataGeracao,
        String modelo,
        RelatorioAnaliseDTO relatorio
) {

    public AnaliseIAResponseDTO(AnaliseIA analise, RelatorioAnaliseDTO relatorio) {
        this(
                analise.getId(),
                analise.getAnimal() != null ? analise.getAnimal().getId() : null,
                analise.getAnimal() != null ? analise.getAnimal().getBrinco() : null,
                analise.getLote() != null ? analise.getLote().getId() : null,
                analise.getLote() != null ? analise.getLote().getNumero() : null,
                analise.getTipo(),
                analise.getDataGeracao(),
                analise.getModeloUtilizado(),
                relatorio
        );
    }
}
