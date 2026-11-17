package com.pecucore.system.core.dto.custo;

import com.pecucore.system.core.model.TipoCusto;
import com.pecucore.system.core.service.custo.RateioCustoAnimal;

import java.util.Map;

public record RateioCustoResponseDTO(
        Long animalId,
        int brinco,
        double total,
        double custoDireto,
        double custoRateadoLote,
        double custoRateadoPropriedade,
        Map<TipoCusto, Double> totalPorTipo
) {

    public RateioCustoResponseDTO(RateioCustoAnimal rateio) {
        this(
            rateio.animal().getId(),
            rateio.animal().getBrinco(),
            rateio.total(),
            rateio.custoDireto(),
            rateio.custoRateadoLote(),
            rateio.custoRateadoPropriedade(),
            rateio.totalPorTipo()
    );
    }
}
