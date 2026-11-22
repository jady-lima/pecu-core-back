package com.pecucore.system.core.extensao.desempenho;

import com.pecucore.system.core.extensao.PontoFlexivel;
import com.pecucore.system.core.model.Pesagem;

import java.time.LocalDate;

@PontoFlexivel
public interface IndicadorDesempenho {

    String getNome();

    String getUnidade();

    Double calcular(Pesagem pesagemAnterior, double pesoAtualKg, LocalDate dataAtual);

    Double calcularHistorico(Pesagem primeiraPesagem, Pesagem ultimaPesagem);

    EscalaCondicaoCorporal getEscalaCondicaoCorporal();

    /**
     * Pontuação para ordenar os animais no ranking do lote: maior pontuação = melhor posição.
     * Cada aplicação decide o que é "melhor" (ex.: corte = maior ganho; manutenção = ficar na faixa).
     * A meta pode ser null quando o lote não tem meta.
     */
    double calcularPontuacaoRanking(double valor, Meta meta);
}