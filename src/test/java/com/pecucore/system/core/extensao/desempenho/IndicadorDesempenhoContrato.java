package com.pecucore.system.core.extensao.desempenho;

import com.pecucore.system.core.model.Pesagem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contrato do ponto flexível IndicadorDesempenho: o que o núcleo espera de qualquer implementação.
 * Cada aplicação estende esta classe e informa a sua implementação em criarIndicador().
 *
 * Uso no núcleo:
 * - PesagemService: calcular(ultimaPesagem, peso, data); na primeira pesagem, ultimaPesagem é null.
 * - DesempenhoService, ContextoAnaliseService, ContextoComparativoService: calcularHistorico(primeira, última);
 *   null significa "não foi possível calcular" e vira mensagem de erro para o usuário.
 * - PesagemService e PromptAnaliseBuilder: getEscalaCondicaoCorporal() valida e descreve o escore corporal.
 */
public abstract class IndicadorDesempenhoContrato {

    private static final LocalDate DATA = LocalDate.of(2026, 9, 1);
    private static final double DELTA = 1e-9;

    protected IndicadorDesempenho indicador;

    protected abstract IndicadorDesempenho criarIndicador();

    @BeforeEach
    public void prepararIndicador() {
        indicador = criarIndicador();
    }

    protected static Pesagem pesagem(LocalDate data, double pesoKg) {
        Pesagem pesagem = new Pesagem();
        pesagem.setData(data);
        pesagem.setPesoKg(pesoKg);
        return pesagem;
    }

    @Test
    @DisplayName("Nome e unidade do indicador estão preenchidos (aparecem nas telas e na análise de IA)")
    public void nomeEUnidadePreenchidos() {
        assertFalse(indicador.getNome() == null || indicador.getNome().isBlank());
        assertFalse(indicador.getUnidade() == null || indicador.getUnidade().isBlank());
    }

    @Test
    @DisplayName("A escala de condição corporal existe e aceita os próprios limites")
    public void escalaCondicaoCorporalConsistente() {
        EscalaCondicaoCorporal escala = indicador.getEscalaCondicaoCorporal();

        assertNotNull(escala);
        assertTrue(escala.aceita(escala.minimo()));
        assertTrue(escala.aceita(escala.maximo()));
        assertFalse(escala.aceita(escala.maximo() + escala.passo()));
    }

    @Test
    @DisplayName("Primeira pesagem do animal (sem anterior): índice não calculado, sem erro")
    public void primeiraPesagemSemIndice() {
        assertNull(indicador.calcular(null, 300.0, DATA));
    }

    @Test
    @DisplayName("Pesagens na mesma data ou ausentes: índice não calculado, sem erro")
    public void semIntervaloSemIndice() {
        Pesagem pesagem = pesagem(DATA, 300.0);

        assertNull(indicador.calcular(pesagem, 310.0, DATA));
        assertNull(indicador.calcularHistorico(pesagem, pesagem(DATA, 310.0)));
        assertNull(indicador.calcularHistorico(null, pesagem));
        assertNull(indicador.calcularHistorico(pesagem, null));
    }

    @Test
    @DisplayName("Pesagens em datas diferentes sempre produzem índice (ganho, perda ou peso estável)")
    public void datasDiferentesProduzemIndice() {
        Pesagem inicio = pesagem(DATA, 300.0);
        LocalDate fim = DATA.plusDays(30);

        assertNotNull(indicador.calcularHistorico(inicio, pesagem(fim, 330.0)));
        assertNotNull(indicador.calcularHistorico(inicio, pesagem(fim, 270.0)));
        assertNotNull(indicador.calcularHistorico(inicio, pesagem(fim, 300.0)));
    }

    @Test
    @DisplayName("Índice de uma pesagem = índice do período desde a pesagem anterior")
    public void calcularEHistoricoConcordam() {
        Pesagem anterior = pesagem(DATA, 300.0);
        Pesagem atual = pesagem(DATA.plusDays(15), 318.0);

        Double daPesagem = indicador.calcular(anterior, atual.getPesoKg(), atual.getData());
        Double doPeriodo = indicador.calcularHistorico(anterior, atual);

        assertNotNull(daPesagem);
        assertNotNull(doPeriodo);
        assertEquals(doPeriodo, daPesagem, DELTA);
    }

    @Test
    @DisplayName("Pontuação do ranking existe para qualquer valor, com ou sem meta")
    public void pontuacaoSempreDefinida() {
        for (Meta meta : new Meta[] {null, Meta.minima(0.5), Meta.faixa(0.2, 0.6)}) {
            for (double valor : new double[] {-1.0, 0.0, 0.3, 0.5, 2.0}) {
                double pontuacao = indicador.calcularPontuacaoRanking(valor, meta);

                assertTrue(Double.isFinite(pontuacao), "Valor " + valor + " com meta " + meta);
            }
        }
    }

    @Test
    @DisplayName("No ranking, um valor abaixo da meta fica sempre atrás de um valor dentro da meta")
    public void abaixoDaMetaFicaAtras() {
        verificarAbaixoAtrasDeDentro(Meta.minima(0.5),
                new double[] {-1.0, 0.0, 0.49}, new double[] {0.5, 0.8, 2.0});
        verificarAbaixoAtrasDeDentro(Meta.faixa(0.2, 0.6),
                new double[] {-1.0, 0.0, 0.19}, new double[] {0.2, 0.4, 0.6});
    }

    private void verificarAbaixoAtrasDeDentro(Meta meta, double[] valoresAbaixo, double[] valoresDentro) {
        for (double abaixo : valoresAbaixo) {
            assertEquals(ResultadoMeta.ABAIXO, meta.avaliar(abaixo));

            for (double dentro : valoresDentro) {
                assertEquals(ResultadoMeta.DENTRO, meta.avaliar(dentro));
                assertTrue(
                        indicador.calcularPontuacaoRanking(abaixo, meta) < indicador.calcularPontuacaoRanking(dentro, meta),
                        "Abaixo (" + abaixo + ") não ficou atrás de dentro (" + dentro + ") com meta " + meta);
            }
        }
    }

}