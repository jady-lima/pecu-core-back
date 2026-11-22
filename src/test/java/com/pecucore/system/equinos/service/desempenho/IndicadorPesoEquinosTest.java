package com.pecucore.system.equinos.service.desempenho;

import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.model.Pesagem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IndicadorPesoEquinos: variação de peso e ranking por proximidade da meta")
class IndicadorPesoEquinosTest {

    private static final double DELTA = 1e-9;
    private static final Meta MANUTENCAO = Meta.faixa(-0.1, 0.1);

    private final IndicadorPesoEquinos indicador = new IndicadorPesoEquinos();

    private static Pesagem pesagem(LocalDate data, double pesoKg) {
        Pesagem pesagem = new Pesagem();
        pesagem.setData(data);
        pesagem.setPesoKg(pesoKg);
        return pesagem;
    }

    @Test
    @DisplayName("Requisito: variação de peso por dia, positiva ou negativa")
    void variacaoDePeso() {
        LocalDate inicio = LocalDate.of(2026, 9, 1);

        assertEquals(1.0, indicador.calcularHistorico(pesagem(inicio, 300.0), pesagem(inicio.plusDays(30), 330.0)), DELTA);
        assertEquals(-0.5, indicador.calcularHistorico(pesagem(inicio, 300.0), pesagem(inicio.plusDays(30), 285.0)), DELTA);
    }

    @Nested
    @DisplayName("Requisito: no ranking, melhor é ficar dentro da faixa da meta")
    class Ranking {

        @Test
        @DisplayName("Dentro da faixa: pontuação máxima (0)")
        void dentroDaFaixa() {
            assertEquals(0.0, indicador.calcularPontuacaoRanking(0.05, MANUTENCAO), DELTA);
        }

        @Test
        @DisplayName("Fora da faixa: perde pontos pela distância até o limite")
        void foraDaFaixa() {
            assertEquals(-0.2, indicador.calcularPontuacaoRanking(0.3, MANUTENCAO), DELTA);
            assertEquals(-0.3, indicador.calcularPontuacaoRanking(-0.4, MANUTENCAO), DELTA);
        }

        @Test
        @DisplayName("Diferente dos bovinos: adulto que ganha muito peso fica atrás de quem manteve o peso")
        void ganharMuitoNaoEhMelhor() {
            assertTrue(indicador.calcularPontuacaoRanking(0.0, MANUTENCAO)
                    > indicador.calcularPontuacaoRanking(0.5, MANUTENCAO));
        }

        @Test
        @DisplayName("Sem meta não há critério: todos empatam")
        void semMetaEmpate() {
            assertEquals(0.0, indicador.calcularPontuacaoRanking(0.5, null), DELTA);
            assertEquals(0.0, indicador.calcularPontuacaoRanking(-0.5, null), DELTA);
        }
    }
}