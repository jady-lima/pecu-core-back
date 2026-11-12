package com.pecucore.system.bovinos.sanidade;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CalculadoraCarencia {

    public LocalDate calcularDataFimCarencia(LocalDate dataAplicacao, int carenciaDias) {
        return dataAplicacao.plusDays(carenciaDias);
    }
}
