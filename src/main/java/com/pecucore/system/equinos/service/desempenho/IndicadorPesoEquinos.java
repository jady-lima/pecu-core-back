package com.pecucore.system.equinos.service.desempenho;

import com.pecucore.system.core.extensao.desempenho.EscalaCondicaoCorporal;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.model.Pesagem;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Indicador dos equinos: variação de peso por dia (kg/dia) entre duas pesagens.
 * A conta é a mesma do GMD, mas o critério de "melhor" é outro: o objetivo é ficar dentro da faixa
 * da meta da fase (crescer na medida certa no potro, manter o peso no adulto), e não ganhar o máximo.
 * Escala de condição corporal: Henneke et al. (1983), Equine Veterinary Journal 15(4), de 1 a 9;
 * na prática também se usa meio ponto.
 */
@Component
public class IndicadorPesoEquinos implements IndicadorDesempenho {

    private static final String NOME = "Variação de peso";
    private static final String UNIDADE = "kg/dia";
    private static final EscalaCondicaoCorporal ESCALA_ECC = new EscalaCondicaoCorporal(1, 9, 0.5);

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

    /**
     * Dentro da faixa da meta = 0 (melhor); fora dela, quanto mais longe, mais negativo.
     * Sem meta não há critério de comparação: todos empatam (pontuação 0).
     */
    @Override
    public double calcularPontuacaoRanking(double valor, Meta meta) {
        if (meta == null) {
            return 0.0;
        }

        return switch (meta.avaliar(valor)) {
            case ABAIXO -> valor - meta.minimo();
            case ACIMA -> meta.maximo() - valor;
            case DENTRO -> 0.0;
        };
    }

    private Double calcularPorDia(double pesoInicialKg, double pesoFinalKg, LocalDate dataInicial, LocalDate dataFinal) {
        long dias = ChronoUnit.DAYS.between(dataInicial, dataFinal);

        if (dias <= 0) {
            return null;
        }

        return (pesoFinalKg - pesoInicialKg) / dias;
    }
}