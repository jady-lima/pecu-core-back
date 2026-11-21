package com.pecucore.system.bovinos.service.desempenho;

import com.pecucore.system.core.extensao.desempenho.EscalaCondicaoCorporal;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.model.Pesagem;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class GmdService implements IndicadorDesempenho {

    private static final String NOME = "GMD";
    private static final String UNIDADE = "kg/dia";
    private static final EscalaCondicaoCorporal ESCALA_ECC = new EscalaCondicaoCorporal(1, 5, 0.5);

    @Override
    public String getNome() {
        return NOME;
    }

    @Override
    public String getUnidade() {
        return UNIDADE;
    }

    @Override
    public EscalaCondicaoCorporal getEscalaCondicaoCorporal() {
        return ESCALA_ECC;
    }

    @Override
    public Double calcular(Pesagem pesagemAnterior, double pesoAtualKg, LocalDate dataAtual) {
        if (pesagemAnterior == null) {
            return null;
        }

        return calcularPorDia(pesagemAnterior.getPesoKg(), pesoAtualKg, pesagemAnterior.getData(), dataAtual);
    }

    @Override
    public Double calcularHistorico(Pesagem primeiraPesagem, Pesagem ultimaPesagem) {
        if (primeiraPesagem == null || ultimaPesagem == null) {
            return null;
        }

        return calcularPorDia(
            primeiraPesagem.getPesoKg(),
            ultimaPesagem.getPesoKg(),
            primeiraPesagem.getData(),
            ultimaPesagem.getData()
        );
    }

    /** Bovinos de corte: quanto maior o ganho de peso, melhor a posição, com ou sem meta. */
    @Override
    public double calcularPontuacaoRanking(double valor, Meta meta) {
        return valor;
    }

    private Double calcularPorDia(double pesoInicialKg, double pesoFinalKg, LocalDate dataInicial, LocalDate dataFinal) {
        long dias = ChronoUnit.DAYS.between(dataInicial, dataFinal);

        if (dias <= 0) {
            return null;
        }

        return (pesoFinalKg - pesoInicialKg) / dias;
    }
}
