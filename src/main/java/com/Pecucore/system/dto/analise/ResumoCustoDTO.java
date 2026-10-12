package com.Pecucore.system.dto.analise;

import com.Pecucore.system.model.Custo;
import com.Pecucore.system.model.TipoCusto;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record ResumoCustoDTO(double total, Map<TipoCusto, Double> totalPorTipo) {

    public ResumoCustoDTO(List<Custo> custos) {
        this(
                custos.stream().mapToDouble(Custo::getValor).sum(),
                totalizarPorTipo(custos)
        );
    }

    private static Map<TipoCusto, Double> totalizarPorTipo(List<Custo> custos) {
        Map<TipoCusto, Double> totais = new EnumMap<>(TipoCusto.class);

        for (Custo custo : custos) {
            totais.merge(custo.getTipo(), custo.getValor(), Double::sum);
        }
        
        return totais;
    }
}
