package com.Pecucore.system.service.calculo;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculadoraEstatisticasLote {

    public double calcularMedia(List<Double> valores) {
        return valores.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    }

    public double calcularMediana(List<Double> valores) {
        List<Double> valoresOrdenados = valores.stream().sorted().toList();
        int meio = valoresOrdenados.size() / 2;
        
        if (valoresOrdenados.size() % 2 == 0) {
            return (valoresOrdenados.get(meio - 1) + valoresOrdenados.get(meio)) / 2;
        }

        return valoresOrdenados.get(meio);
    }

    public int calcularPosicao(double valor, List<Double> demaisValores) {
        long valoresMaiores = demaisValores.stream().filter(outro -> outro > valor).count();

        return (int) valoresMaiores + 1;
    }

    public double calcularPercentil(double valor, List<Double> demaisValores) {
        long valoresMenores = demaisValores.stream().filter(outro -> outro < valor).count();

        return (valoresMenores * 100.0) / demaisValores.size();
    }

    public Double calcularPercentualDaReferencia(double valor, double referencia) {
        if (referencia <= 0) {
            return null;
        }

        return (valor / referencia) * 100;
    }
}
