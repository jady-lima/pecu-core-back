package com.pecucore.system.core.service.desempenho;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.animal.AnimalService;
import com.pecucore.system.core.service.calculo.CalculadoraEstatisticasLote;
import com.pecucore.system.core.service.lote.LoteService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Regressão do DesempenhoService (núcleo).
 * O indicador é um mock: o núcleo não conhece a espécie, só o ponto flexível IndicadorDesempenho.
 * A calculadora é real (@Spy), para os percentuais serem calculados de verdade.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DesempenhoService: comparação do desempenho do animal com a meta")
class DesempenhoServiceTest {

    private static final int BRINCO = 101;
    private static final LocalDate INICIO = LocalDate.of(2026, 9, 1);
    private static final LocalDate FIM = INICIO.plusDays(30);
    private static final double DELTA = 1e-9;

    @Mock
    private AnimalService animalService;

    @Mock
    private PesagemService pesagemService;

    @Mock
    private IndicadorDesempenho indicadorDesempenho;

    @Mock
    private LoteService loteService;

    @Spy
    private CalculadoraEstatisticasLote calculadoraEstatisticasLote;

    @InjectMocks
    private DesempenhoService desempenhoService;

    private Usuario usuario;
    private Lote lote;
    private Animal animal;
    private Pesagem primeiraPesagem;
    private Pesagem ultimaPesagem;

    @BeforeEach
    void setUp() {
        usuario = mock(Usuario.class);
        lote = new Lote();

        animal = new Animal();
        animal.setBrinco(BRINCO);
        animal.setLote(lote);

        primeiraPesagem = pesagem(INICIO, 300.0);
        ultimaPesagem = pesagem(FIM, 330.0);

        lenient().when(animalService.getAnimalByBrinco(BRINCO, usuario)).thenReturn(animal);
        lenient().when(indicadorDesempenho.getNome()).thenReturn("Índice de teste");
        lenient().when(indicadorDesempenho.getUnidade()).thenReturn("un/dia");
    }

    private static Pesagem pesagem(LocalDate data, double pesoKg) {
        Pesagem pesagem = new Pesagem();
        pesagem.setData(data);
        pesagem.setPesoKg(pesoKg);
        return pesagem;
    }

    private void dadoHistorico(Pesagem... pesagens) {
        when(pesagemService.getHistorico(BRINCO, usuario)).thenReturn(List.of(pesagens));
    }

    private void dadoIndiceDoAnimal(Double indice) {
        when(indicadorDesempenho.calcularHistorico(primeiraPesagem, ultimaPesagem)).thenReturn(indice);
    }

    private void dadaMetaDoLote(Double meta) {
        when(loteService.getMetaDesempenho(lote)).thenReturn(Optional.ofNullable(meta));
    }

    @Nested
    @DisplayName("Requisito: comparar o índice do animal com a meta do lote")
    class CompararComMeta {

        @Test
        @DisplayName("Índice abaixo da meta: marca abaixoDaMeta e calcula diferença e percentuais")
        void indiceAbaixoDaMeta() {
            dadoHistorico(primeiraPesagem, ultimaPesagem);
            dadoIndiceDoAnimal(0.8);
            dadaMetaDoLote(1.0);

            ResultadoComparacaoDesempenho resultado = desempenhoService.comparar(BRINCO, usuario);

            assertSame(animal, resultado.animal());
            assertEquals("Índice de teste", resultado.indicador());
            assertEquals("un/dia", resultado.unidade());
            assertEquals(0.8, resultado.indiceAtual(), DELTA);
            assertEquals(1.0, resultado.indiceEsperado(), DELTA);
            assertEquals(-0.2, resultado.diferenca(), DELTA);
            assertEquals(80.0, resultado.percentualDaMeta(), DELTA);
            assertEquals(20.0, resultado.percentualAbaixoDaMeta(), DELTA);
            assertTrue(resultado.abaixoDaMeta());
        }

        @Test
        @DisplayName("Índice acima da meta: não marca abaixoDaMeta")
        void indiceAcimaDaMeta() {
            dadoHistorico(primeiraPesagem, ultimaPesagem);
            dadoIndiceDoAnimal(1.2);
            dadaMetaDoLote(1.0);

            ResultadoComparacaoDesempenho resultado = desempenhoService.comparar(BRINCO, usuario);

            assertEquals(0.2, resultado.diferenca(), DELTA);
            assertEquals(120.0, resultado.percentualDaMeta(), DELTA);
            assertEquals(-20.0, resultado.percentualAbaixoDaMeta(), DELTA);
            assertFalse(resultado.abaixoDaMeta());
        }

        @Test
        @DisplayName("Índice igual à meta conta como meta atingida")
        void indiceIgualAMeta() {
            dadoHistorico(primeiraPesagem, ultimaPesagem);
            dadoIndiceDoAnimal(1.0);
            dadaMetaDoLote(1.0);

            ResultadoComparacaoDesempenho resultado = desempenhoService.comparar(BRINCO, usuario);

            assertEquals(100.0, resultado.percentualDaMeta(), DELTA);
            assertFalse(resultado.abaixoDaMeta());
        }

        @Test
        @DisplayName("Usa a primeira e a última pesagem do histórico, ignorando as intermediárias")
        void usaPrimeiraEUltimaPesagem() {
            Pesagem intermediaria = pesagem(INICIO.plusDays(15), 310.0);
            dadoHistorico(primeiraPesagem, intermediaria, ultimaPesagem);
            dadoIndiceDoAnimal(1.0);
            dadaMetaDoLote(1.0);

            desempenhoService.comparar(BRINCO, usuario);

            verify(indicadorDesempenho).calcularHistorico(primeiraPesagem, ultimaPesagem);
            verify(indicadorDesempenho, never()).calcularHistorico(primeiraPesagem, intermediaria);
        }
    }

    @Nested
    @DisplayName("Requisito: recusar a comparação quando faltam dados")
    class DadosInsuficientes {

        @Test
        @DisplayName("Menos de duas pesagens: erro de conflito, sem calcular o índice")
        void menosDeDuasPesagens() {
            dadoHistorico(primeiraPesagem);

            assertThrows(ConflitoNegocioException.class,
                    () -> desempenhoService.comparar(BRINCO, usuario));
            verify(indicadorDesempenho, never()).calcularHistorico(any(), any());
        }

        @Test
        @DisplayName("Indicador não consegue calcular: erro com o nome do indicador")
        void indicadorSemResultado() {
            dadoHistorico(primeiraPesagem, ultimaPesagem);
            dadoIndiceDoAnimal(null);

            ConflitoNegocioException erro = assertThrows(ConflitoNegocioException.class,
                    () -> desempenhoService.comparar(BRINCO, usuario));
            assertTrue(erro.getMessage().contains("Índice de teste"));
        }

        @Test
        @DisplayName("Sem meta no lote nem na fase: recurso não encontrado")
        void semMeta() {
            dadoHistorico(primeiraPesagem, ultimaPesagem);
            dadoIndiceDoAnimal(0.8);
            dadaMetaDoLote(null);

            assertThrows(RecursoNaoEncontradoException.class,
                    () -> desempenhoService.comparar(BRINCO, usuario));
        }
    }

    @Nested
    @DisplayName("Bug conhecido (será corrigido no Passo 5)")
    class BugConhecido {

        @Test
        @Disabled("DesempenhoService:68 guarda em double o retorno de calcularPercentualDaReferencia, "
                + "que é null quando a meta é zero ou negativa: NullPointerException (erro 500). "
                + "Nos bovinos a meta é sempre positiva; em outras espécies pode não ser.")
        @DisplayName("Meta zero ou negativa não pode derrubar a comparação")
        void metaNaoPositivaNaoCausaErro500() {
            dadoHistorico(primeiraPesagem, ultimaPesagem);
            dadoIndiceDoAnimal(0.05);
            dadaMetaDoLote(0.0);

            assertDoesNotThrow(() -> desempenhoService.comparar(BRINCO, usuario));
        }
    }
}