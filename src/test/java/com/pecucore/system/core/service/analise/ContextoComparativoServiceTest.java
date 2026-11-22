package com.pecucore.system.core.service.analise;

import com.pecucore.system.core.dto.analise.ContextoAnaliseAnimalDTO;
import com.pecucore.system.core.dto.analise.ContextoAnaliseComparativaDTO;
import com.pecucore.system.core.dto.analise.DesempenhoContextoDTO;
import com.pecucore.system.core.dto.analise.EstatisticasLoteDTO;
import com.pecucore.system.core.dto.analise.ParLoteDTO;
import com.pecucore.system.core.dto.analise.PesagemContextoDTO;
import com.pecucore.system.core.dto.analise.PosicaoAnimalLoteDTO;
import com.pecucore.system.core.exception.DadosInsuficientesException;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.extensao.desempenho.Meta;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.animal.AnimalConsultaService;
import com.pecucore.system.core.service.animal.AnimalService;
import com.pecucore.system.core.service.calculo.CalculadoraEstatisticasLote;
import com.pecucore.system.core.service.custo.RateioCustoService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import com.pecucore.system.core.service.politica.PoliticaVisibilidadeFinanceira;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Regressão do ContextoComparativoService (núcleo).
 * O indicador é falso (variação de peso por dia), para o teste não depender de espécie.
 * Lote do cenário principal: animal analisado com índice 1,0 e três pares com 1,5, 0,8 e 0,5.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ContextoComparativoService: posição do animal em relação ao lote")
class ContextoComparativoServiceTest {

    private static final Long ANIMAL_ID = 1L;
    private static final Long LOTE_ID = 10L;
    private static final LocalDate INICIO = LocalDate.of(2026, 9, 1);
    private static final LocalDate FIM = INICIO.plusDays(30);
    private static final double DELTA = 1e-9;

    @Mock
    private AnimalService animalService;

    @Mock
    private AnimalConsultaService animalConsultaService;

    @Mock
    private ContextoAnaliseService contextoAnaliseService;

    @Mock
    private IndicadorDesempenho indicadorDesempenho;

    @Spy
    private CalculadoraEstatisticasLote calculadoraEstatisticasLote;

    @Mock
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira;

    @Mock
    private PesagemService pesagemService;

    @Mock
    private RateioCustoService rateioCustoService;

    @InjectMocks
    private ContextoComparativoService contextoComparativoService;

    private Usuario usuario;
    private Animal animalAnalisado;
    private final List<Pesagem> pesagensDoLote = new ArrayList<>();

    @BeforeEach
    void setUp() {
        usuario = mock(Usuario.class);

        Lote lote = mock(Lote.class);
        lenient().when(lote.getId()).thenReturn(LOTE_ID);

        animalAnalisado = animal(ANIMAL_ID, 330.0);
        lenient().when(animalAnalisado.getLote()).thenReturn(lote);
        lenient().when(animalService.getAnimalById(ANIMAL_ID, usuario)).thenReturn(animalAnalisado);

        lenient().when(indicadorDesempenho.getNome()).thenReturn("Índice de teste");
        lenient().when(indicadorDesempenho.calcularHistorico(any(Pesagem.class), any(Pesagem.class)))
                .thenAnswer(invocacao -> {
                    Pesagem primeira = invocacao.getArgument(0);
                    Pesagem ultima = invocacao.getArgument(1);
                    long dias = ChronoUnit.DAYS.between(primeira.getData(), ultima.getData());
                    return dias <= 0 ? null : (ultima.getPesoKg() - primeira.getPesoKg()) / dias;
                });


        // Critério de ranking do indicador falso: maior índice = melhor (o mesmo dos bovinos).
        lenient().when(indicadorDesempenho.calcularPontuacaoRanking(anyDouble(), any()))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        lenient().when(pesagemService.getPesagensAnimaisAtivosByLoteId(LOTE_ID)).thenReturn(pesagensDoLote);
        lenient().when(animalConsultaService.countAnimaisAtivosByLoteId(LOTE_ID)).thenReturn(4L);
    }

    private static Animal animal(Long id, double pesoAtual) {
        Animal animal = mock(Animal.class);
        lenient().when(animal.getId()).thenReturn(id);
        lenient().when(animal.getPesoAtual()).thenReturn(pesoAtual);
        return animal;
    }

    private static Pesagem pesagem(Animal animal, LocalDate data, double pesoKg) {
        Pesagem pesagem = new Pesagem();
        pesagem.setAnimal(animal);
        pesagem.setData(data);
        pesagem.setPesoKg(pesoKg);
        return pesagem;
    }

    private void dadoAnimalAnalisadoComIndice(Double indice) {
        DesempenhoContextoDTO desempenho =
                new DesempenhoContextoDTO("Índice de teste", "un/dia", indice, 30L, 0.9, null, null, null);

        List<PesagemContextoDTO> pesagens = List.of(
                new PesagemContextoDTO(INICIO, 300.0, null, null),
                new PesagemContextoDTO(FIM, 330.0, null, null)
        );

        ContextoAnaliseAnimalDTO contexto = new ContextoAnaliseAnimalDTO(
                FIM, null, desempenho, pesagens, List.of(), List.of(), List.of(), List.of(), null);

        when(contextoAnaliseService.montarIndividual(ANIMAL_ID, usuario)).thenReturn(contexto);
    }

    private void dadoOutroAnimal(Long id, LocalDate data1, double peso1, LocalDate data2, double peso2) {
        Animal animal = animal(id, peso2);
        pesagensDoLote.add(pesagem(animal, data1, peso1));
        pesagensDoLote.add(pesagem(animal, data2, peso2));
    }

    @Nested
    @DisplayName("Requisito: comparar o animal com os demais do lote no mesmo período")
    class Comparacao {

        @BeforeEach
        void loteComTresPares() {
            pesagensDoLote.add(pesagem(animalAnalisado, INICIO, 300.0));
            pesagensDoLote.add(pesagem(animalAnalisado, FIM, 330.0));
            dadoOutroAnimal(2L, INICIO, 300.0, FIM, 345.0); // 1,5 por dia
            dadoOutroAnimal(3L, INICIO, 300.0, FIM, 324.0); // 0,8 por dia
            dadoOutroAnimal(4L, INICIO, 300.0, FIM, 315.0); // 0,5 por dia
        }

        @Test
        @DisplayName("Estatísticas do lote incluem o animal analisado e os pares comparáveis")
        void estatisticasDoLote() {
            dadoAnimalAnalisadoComIndice(1.0);

            EstatisticasLoteDTO estatisticas =
                    contextoComparativoService.montarComparativo(ANIMAL_ID, usuario).estatisticasLote();

            assertEquals(4, estatisticas.totalAnimaisAtivos());
            assertEquals(4, estatisticas.animaisComparados());
            assertEquals(0.95, estatisticas.indiceMedio(), DELTA);
            assertEquals(0.9, estatisticas.indiceMediano(), DELTA);
            assertEquals(0.5, estatisticas.indiceMinimo(), DELTA);
            assertEquals(1.5, estatisticas.indiceMaximo(), DELTA);
            assertEquals(0.9, estatisticas.indiceMeta(), DELTA);
            assertEquals(328.5, estatisticas.pesoMedioKg(), DELTA);
        }

        @Test
        @DisplayName("Posição no ranking segue o critério da aplicação (aqui: maior índice = melhor)")
        void posicaoNoRanking() {
            dadoAnimalAnalisadoComIndice(1.0);

            PosicaoAnimalLoteDTO posicao =
                    contextoComparativoService.montarComparativo(ANIMAL_ID, usuario).posicaoNoLote();

            assertEquals(2, posicao.posicaoIndice());
            assertEquals(4, posicao.totalComparados());
            assertEquals(200.0 / 3, posicao.percentilIndice(), DELTA);
            assertEquals(0.05, posicao.diferencaIndiceMedia(), DELTA);
            assertEquals(1.0 / 0.95 * 100, posicao.percentualDaMediaIndice(), DELTA);
            assertEquals(1.5, posicao.diferencaPesoMedioKg(), DELTA);
        }

        @Test
        @DisplayName("Pares listados do maior para o menor índice, sem repetir o animal analisado")
        void paresOrdenados() {
            dadoAnimalAnalisadoComIndice(1.0);

            List<ParLoteDTO> pares =
                    contextoComparativoService.montarComparativo(ANIMAL_ID, usuario).demaisAnimais();

            assertEquals(List.of(1.5, 0.8, 0.5),
                    pares.stream().map(ParLoteDTO::indicePeriodo).toList());
        }

        @Test
        @DisplayName("Ignora pares sem pesagens no período ou que cobrem menos da metade dele")
        void ignoraParesForaDoPeriodo() {
            dadoOutroAnimal(5L, INICIO.minusDays(40), 280.0, INICIO.minusDays(1), 299.0);
            dadoOutroAnimal(6L, INICIO, 300.0, INICIO.plusDays(5), 310.0);
            dadoAnimalAnalisadoComIndice(1.0);

            ContextoAnaliseComparativaDTO comparativo =
                    contextoComparativoService.montarComparativo(ANIMAL_ID, usuario);

            assertEquals(4, comparativo.estatisticasLote().animaisComparados());
            assertEquals(3, comparativo.demaisAnimais().size());
        }

        @Test
        @DisplayName("Custo médio fica oculto para quem não pode ver dados financeiros")
        void custoOcultoSemPermissao() {
            dadoAnimalAnalisadoComIndice(1.0);
            when(politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuario)).thenReturn(false);

            EstatisticasLoteDTO estatisticas =
                    contextoComparativoService.montarComparativo(ANIMAL_ID, usuario).estatisticasLote();

            assertNull(estatisticas.custoMedioPorAnimal());
            verifyNoInteractions(rateioCustoService);
        }

        @Test
        @DisplayName("Custo médio aparece para quem pode ver dados financeiros")
        void custoVisivelComPermissao() {
            dadoAnimalAnalisadoComIndice(1.0);
            when(politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuario)).thenReturn(true);
            when(rateioCustoService.calcularCustoMedio(anyList())).thenReturn(120.0);

            EstatisticasLoteDTO estatisticas =
                    contextoComparativoService.montarComparativo(ANIMAL_ID, usuario).estatisticasLote();

            assertEquals(120.0, estatisticas.custoMedioPorAnimal(), DELTA);
        }


        @Test
        @DisplayName("Com outro critério de ranking da aplicação, a posição e a ordem mudam")
        void rankingSegueCriterioDaAplicacao() {
            // Indicador de manutenção simulado: melhor = mais perto de 1,1.
            when(indicadorDesempenho.calcularPontuacaoRanking(anyDouble(), any()))
                    .thenAnswer(invocacao -> {
                        Double indice = invocacao.getArgument(0);
                        return -Math.abs(indice - 1.1);
                    });
            dadoAnimalAnalisadoComIndice(1.0);

            ContextoAnaliseComparativaDTO comparativo =
                    contextoComparativoService.montarComparativo(ANIMAL_ID, usuario);

            assertEquals(1, comparativo.posicaoNoLote().posicaoIndice());
            assertEquals(100.0, comparativo.posicaoNoLote().percentilIndice(), DELTA);
            assertEquals(List.of(0.8, 1.5, 0.5),
                    comparativo.demaisAnimais().stream().map(ParLoteDTO::indicePeriodo).toList());
        }

        @Test
        @DisplayName("A meta do lote é passada ao critério de ranking da aplicação")
        void metaPassadaAoRanking() {
            dadoAnimalAnalisadoComIndice(1.0);

            contextoComparativoService.montarComparativo(ANIMAL_ID, usuario);

            verify(indicadorDesempenho, times(4)).calcularPontuacaoRanking(anyDouble(), eq(Meta.minima(0.9)));
        }
    }

    @Nested
    @DisplayName("Requisito: recusar a comparação quando faltam dados")
    class DadosInsuficientes {

        @Test
        @DisplayName("Animal sem lote: não há com quem comparar")
        void animalSemLote() {
            doReturn(null).when(animalAnalisado).getLote();

            assertThrows(DadosInsuficientesException.class,
                    () -> contextoComparativoService.montarComparativo(ANIMAL_ID, usuario));
            verifyNoInteractions(contextoAnaliseService);
        }

        @Test
        @DisplayName("Índice do animal não calculável: erro com o nome do indicador")
        void indiceNaoCalculavel() {
            dadoAnimalAnalisadoComIndice(null);

            DadosInsuficientesException erro = assertThrows(DadosInsuficientesException.class,
                    () -> contextoComparativoService.montarComparativo(ANIMAL_ID, usuario));
            assertTrue(erro.getMessage().contains("Índice de teste"));
        }

        @Test
        @DisplayName("Menos de 3 animais comparáveis no lote: comparação recusada")
        void poucosAnimais() {
            dadoOutroAnimal(2L, INICIO, 300.0, FIM, 345.0);
            dadoAnimalAnalisadoComIndice(1.0);

            DadosInsuficientesException erro = assertThrows(DadosInsuficientesException.class,
                    () -> contextoComparativoService.montarComparativo(ANIMAL_ID, usuario));
            assertTrue(erro.getMessage().contains("pelo menos 3 animais"));
        }
    }
}