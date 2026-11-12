package com.pecucore.system;

import com.pecucore.system.core.service.calculo.CalculadoraEstatisticasLote;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraEstatisticasLoteTest {

    private static final double PRECISAO = 0.0001;

    private final CalculadoraEstatisticasLote calculadora = new CalculadoraEstatisticasLote();

    // ---------- Média e mediana ----------

    @Test
    void deveCalcularMedia() {
        assertEquals(2.0, calculadora.calcularMedia(List.of(1.0, 2.0, 3.0)), PRECISAO);
    }

    @Test
    void mediaDeListaVaziaLancaExcecao() {
        assertThrows(NoSuchElementException.class,
                () -> calculadora.calcularMedia(List.of()));
    }

    @Test
    void deveCalcularMedianaComQuantidadeImpar() {
        assertEquals(2.0, calculadora.calcularMediana(List.of(3.0, 1.0, 2.0)), PRECISAO);
    }

    @Test
    void deveCalcularMedianaComQuantidadePar() {
        assertEquals(2.5, calculadora.calcularMediana(List.of(4.0, 1.0, 3.0, 2.0)), PRECISAO);
    }

    // ---------- Posição no lote ----------

    @Test
    void melhorValorFicaNaPrimeiraPosicao() {
        assertEquals(1, calculadora.calcularPosicao(3.0, List.of(2.0, 1.0)));
    }

    @Test
    void posicaoContaQuantosValoresSaoMaiores() {
        assertEquals(3, calculadora.calcularPosicao(1.0, List.of(2.0, 3.0, 0.5)));
    }

    @Test
    void empateNaoEmpurraOAnimalParaBaixo() {
        assertEquals(2, calculadora.calcularPosicao(1.5, List.of(2.0, 1.5, 1.0)));
    }

    // ---------- Percentil ----------

    @Test
    void percentilContaApenasValoresEstritamenteMenores() {
        assertEquals(50.0,
                calculadora.calcularPercentil(1.5, List.of(1.0, 2.0, 1.2, 1.5)), PRECISAO);
    }

    @Test
    void maiorValorDoLoteTemPercentilCem() {
        assertEquals(100.0, calculadora.calcularPercentil(3.0, List.of(1.0, 2.0)), PRECISAO);
    }

    // ---------- Percentual da referência ----------

    @Test
    void deveCalcularPercentualDaReferencia() {
        assertEquals(80.0, calculadora.calcularPercentualDaReferencia(1.2, 1.5), PRECISAO);
    }

    @Test
    void referenciaZeroNaoTemPercentual() {
        assertNull(calculadora.calcularPercentualDaReferencia(1.2, 0.0));
    }
}