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
}
