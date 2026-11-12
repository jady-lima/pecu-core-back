package com.pecucore.system.bovinos.sanidade;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraCarenciaTest {

    private final CalculadoraCarencia calculadora =
            new CalculadoraCarencia();

    @Test
    void deveCalcularFimDaCarencia() {
        LocalDate data = LocalDate.of(2026, 11, 10);

        LocalDate resultado =
                calculadora.calcularDataFimCarencia(data, 5);

        assertEquals(LocalDate.of(2026, 11, 15), resultado);
    }

    @Test
    void devePermitirCarenciaZero() {
        LocalDate data = LocalDate.of(2026, 11, 10);

        LocalDate resultado =
                calculadora.calcularDataFimCarencia(data, 0);

        assertEquals(data, resultado);
    }
}