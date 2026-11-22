package com.pecucore.system.core.extensao.desempenho;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Meta: faixa de desempenho do núcleo")
class MetaTest {

    @Nested
    @DisplayName("Requisito: classificar um valor em relação à meta")
    class Avaliacao {

        @Test
        @DisplayName("Meta só com mínimo (bovinos): abaixo do mínimo é ABAIXO; o resto é DENTRO")
        void metaMinima() {
            Meta meta = Meta.minima(0.8);

            assertEquals(ResultadoMeta.ABAIXO, meta.avaliar(0.79));
            assertEquals(ResultadoMeta.DENTRO, meta.avaliar(0.8));
            assertEquals(ResultadoMeta.DENTRO, meta.avaliar(5.0));
        }

        @Test
        @DisplayName("Meta em faixa: os limites contam como DENTRO")
        void metaEmFaixa() {
            Meta meta = Meta.faixa(-0.1, 0.1);

            assertEquals(ResultadoMeta.ABAIXO, meta.avaliar(-0.2));
            assertEquals(ResultadoMeta.DENTRO, meta.avaliar(-0.1));
            assertEquals(ResultadoMeta.DENTRO, meta.avaliar(0.0));
            assertEquals(ResultadoMeta.DENTRO, meta.avaliar(0.1));
            assertEquals(ResultadoMeta.ACIMA, meta.avaliar(0.2));
        }

        @Test
        @DisplayName("Meta só com máximo: acima do máximo é ACIMA")
        void metaMaxima() {
            Meta meta = new Meta(null, 1.0);

            assertEquals(ResultadoMeta.DENTRO, meta.avaliar(-3.0));
            assertEquals(ResultadoMeta.ACIMA, meta.avaliar(1.01));
        }
    }

    @Nested
    @DisplayName("Requisito: recusar metas inválidas")
    class Validacao {

        @Test
        @DisplayName("Meta sem mínimo e sem máximo é recusada")
        void semLimites() {
            assertThrows(IllegalArgumentException.class, () -> new Meta(null, null));
        }

        @Test
        @DisplayName("Mínimo maior que o máximo é recusado; mínimo igual ao máximo é aceito")
        void minimoMaiorQueMaximo() {
            assertThrows(IllegalArgumentException.class, () -> Meta.faixa(0.5, 0.2));
            assertDoesNotThrow(() -> Meta.faixa(0.5, 0.5));
        }
    }
}