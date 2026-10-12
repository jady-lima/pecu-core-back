package com.Pecucore.system.service;

import com.Pecucore.system.model.Pesagem;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class GmdService {

    public Double calculateGmd(Pesagem pesagemAnterior, double pesoAtualKg, LocalDate dataAtual) {

        if (pesagemAnterior == null) {
            return null;
        }

        long dias = ChronoUnit.DAYS.between(
                pesagemAnterior.getData(),
                dataAtual
        );

        if (dias <= 0) {
            return null;
        }

        double variacaoKg = pesoAtualKg - pesagemAnterior.getPesoKg();

        return variacaoKg / dias;
    }

    public Double calculateGmdHistorico(Pesagem primeiraPesagem, Pesagem ultimaPesagem) {

        if (primeiraPesagem == null || ultimaPesagem == null) {
            return null;
        }

        long dias = ChronoUnit.DAYS.between(
                primeiraPesagem.getData(),
                ultimaPesagem.getData()
        );

        if (dias <= 0) {
            return null;
        }

        double variacaoKg =
                ultimaPesagem.getPesoKg() - primeiraPesagem.getPesoKg();

        return variacaoKg / dias;
    }
}