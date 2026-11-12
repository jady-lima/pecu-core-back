package com.pecucore.system;

import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.bovinos.desempenho.GmdService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GmdCalculoRegressaoTest {

    private final GmdService gmdService = new GmdService();

    @Test
    void deveCalcularGmdNoRegistroDePesagem() {
        Pesagem anterior = new Pesagem();
        anterior.setPesoKg(300);
        anterior.setData(LocalDate.of(2026, 11, 1));

        Double resultado = gmdService.calculateGmd(
                anterior,
                314,
                LocalDate.of(2026, 11, 8)
        );

        assertEquals(2.0, resultado, 0.0001);
    }

    @Test
    void deveRetornarNullNaPrimeiraPesagem() {
        Double resultado = gmdService.calculateGmd(
                null,
                300,
                LocalDate.of(2026, 11, 8)
        );

        assertNull(resultado);
    }

    @Test
    void deveCalcularPercentualDaMeta() {
        Double resultado =
                gmdService.calcularPercentualDaMeta(1.2, 1.5);

        assertEquals(80.0, resultado, 0.0001);
    }

    @Test
    void deveRetornarNullParaMetaZero() {
        assertNull(
                gmdService.calcularPercentualDaMeta(1.2, 0.0)
        );
    }
}