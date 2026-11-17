package com.pecucore.system.core.dto.analise;

import com.pecucore.system.core.model.TipoCusto;
import com.pecucore.system.core.service.custo.RateioCustoAnimal;

import java.util.Map;

public record ResumoCustoDTO(
        double total,
        double custoDireto,
        double custoRateadoLote,
        double custoRateadoPropriedade,
        Map<TipoCusto, Double> totalPorTipo
) {

    public ResumoCustoDTO(RateioCustoAnimal rateio) {
        this(
                rateio.total(),
                rateio.custoDireto(),
                rateio.custoRateadoLote(),
                rateio.custoRateadoPropriedade(),
                rateio.totalPorTipo()
        );
    }
}
