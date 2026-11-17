package com.pecucore.system.core.service.custo;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoCusto;

import java.util.Map;

public record RateioCustoAnimal(
    Animal animal,
    double custoDireto,
    double custoRateadoLote,
    double custoRateadoPropriedade,
    Map<TipoCusto, Double> totalPorTipo
) {

    public double total() {
        return custoDireto + custoRateadoLote + custoRateadoPropriedade;
    }
}
