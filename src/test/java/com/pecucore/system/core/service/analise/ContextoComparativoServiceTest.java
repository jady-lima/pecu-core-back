package com.pecucore.system.core.service.analise;

import com.pecucore.system.bovinos.desempenho.GmdService;
import com.pecucore.system.core.dto.analise.*;
import com.pecucore.system.core.exception.DadosInsuficientesException;
import com.pecucore.system.core.model.*;
import com.pecucore.system.core.service.AnimalService;
import com.pecucore.system.core.service.calculo.CalculadoraEstatisticasLote;
import com.pecucore.system.core.service.custo.CustoService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import com.pecucore.system.core.service.politica.PoliticaVisibilidadeFinanceira;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextoComparativoServiceTest {

    private static final Long ANIMAL_ID = 1L;
    private static final Long LOTE_ID = 5L;
    private static final LocalDate INICIO = LocalDate.of(2026, 9, 1);
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 12);
    private static final double PRECISAO = 0.001;

    @Mock
    private AnimalService animalService;

    @Mock
    private ContextoAnaliseService contextoAnaliseService;

    @Mock
    private PesagemService pesagemService;

    @Mock
    private CustoService custoService;

    @Spy
    private GmdService gmdService = new GmdService();

    @Spy
    private CalculadoraEstatisticasLote calculadoraEstatisticasLote =
            new CalculadoraEstatisticasLote();

    @Spy
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira =
            new PoliticaVisibilidadeFinanceira();

    @InjectMocks
    private ContextoComparativoService contextoComparativoService;

    private Usuario produtor;
    private Usuario veterinario;
    private Animal analisado;

    @BeforeEach
    void setUp() {
        produtor = usuario(Perfil.PRODUTOR);
        veterinario = usuario(Perfil.VETERINARIO);

        Lote lote = mock(Lote.class);
        lenient().when(lote.getId()).thenReturn(LOTE_ID);

        analisado = animal(ANIMAL_ID, 101, 400);
        lenient().when(analisado.getLote()).thenReturn(lote);

        lenient().when(animalService.getAnimalById(eq(ANIMAL_ID), any(Usuario.class)))
                .thenReturn(analisado);
        lenient().when(animalService.countAnimaisAtivosByLoteId(LOTE_ID)).thenReturn(4L);
        lenient().when(contextoAnaliseService.montarIndividual(eq(ANIMAL_ID), any(Usuario.class)))
                .thenReturn(contextoDoAnimal(1.0));
    }

    // ---------- Estatísticas do lote ----------

    @Test
    void deveCalcularAsEstatisticasDoLote() {
        loteCompleto();

        EstatisticasLoteDTO estatisticas = montar(produtor).estatisticasLote();

        assertEquals(4, estatisticas.totalAnimaisAtivos());
        assertEquals(3, estatisticas.animaisComparados());
        assertEquals(1.1667, estatisticas.gmdMedioKgDia(), PRECISAO);
        assertEquals(1.0, estatisticas.gmdMedianoKgDia(), PRECISAO);
        assertEquals(0.5, estatisticas.gmdMinimoKgDia(), PRECISAO);
        assertEquals(2.0, estatisticas.gmdMaximoKgDia(), PRECISAO);
        assertEquals(1.2, estatisticas.gmdMetaKgDia(), PRECISAO);
        assertEquals(358.333, estatisticas.pesoMedioKg(), PRECISAO);
    }

    @Test
    void deveCalcularAPosicaoDoAnimalNoLote() {
        loteCompleto();

        PosicaoAnimalLoteDTO posicao = montar(produtor).posicaoNoLote();

        assertEquals(2, posicao.posicaoGmd());
        assertEquals(3, posicao.totalComparados());
        assertEquals(50.0, posicao.percentilGmd(), PRECISAO);
        assertEquals(-0.1667, posicao.diferencaGmdMediaKgDia(), PRECISAO);
        assertEquals(85.714, posicao.percentualDaMediaGmd(), PRECISAO);
        assertEquals(41.667, posicao.diferencaPesoMedioKg(), PRECISAO);
    }

    @Test
    void demaisAnimaisFicamOrdenadosPorGmdESemOAnimalAnalisado() {
        loteCompleto();

        List<ParLoteDTO> demais = montar(produtor).demaisAnimais();

        assertEquals(List.of(202, 303), demais.stream().map(ParLoteDTO::brinco).toList());
        assertEquals(2.0, demais.get(0).gmdPeriodoKgDia(), PRECISAO);
        assertEquals(0.5, demais.get(1).gmdPeriodoKgDia(), PRECISAO);
    }

    @Test
    void deveReaproveitarOContextoIndividualDoAnimal() {
        loteCompleto();
        ContextoAnaliseAnimalDTO individual = contextoDoAnimal(1.0);
        when(contextoAnaliseService.montarIndividual(ANIMAL_ID, produtor)).thenReturn(individual);

        ContextoAnaliseComparativaDTO contexto = montar(produtor);

        assertSame(individual, contexto.animalAnalisado());
        assertEquals(HOJE, contexto.dataReferencia());
    }

    // ---------- Visibilidade financeira ----------

    @Test
    void produtorRecebeOCustoMedioDosAnimaisComparados() {
        loteCompleto();
        when(custoService.getCustosByAnimalIds(any())).thenReturn(List.of(custo(90.0), custo(60.0)));

        assertEquals(50.0, montar(produtor).estatisticasLote().custoMedioPorAnimal(), PRECISAO);

        verify(custoService).getCustosByAnimalIds(argThat(ids ->
                ids.size() == 3 && ids.containsAll(List.of(1L, 2L, 3L))));
    }

    @Test
    void veterinarioNaoRecebeOCustoMedio() {
        loteCompleto();

        assertNull(montar(veterinario).estatisticasLote().custoMedioPorAnimal());

        verifyNoInteractions(custoService);
    }

    // ---------- Dados insuficientes ----------

    @Test
    void deveExigirQueOAnimalTenhaLote() {
        when(analisado.getLote()).thenReturn(null);

        assertThrows(DadosInsuficientesException.class, () -> montar(produtor));

        verifyNoInteractions(contextoAnaliseService, pesagemService);
    }

    @Test
    void deveExigirOGmdDoAnimal() {
        when(contextoAnaliseService.montarIndividual(eq(ANIMAL_ID), any(Usuario.class)))
                .thenReturn(contextoDoAnimal(null));

        assertThrows(DadosInsuficientesException.class, () -> montar(produtor));

        verifyNoInteractions(pesagemService);
    }

    @Test
    void deveExigirPeloMenosTresAnimaisComparaveis() {
        Animal rapido = animal(2L, 202, 360);
        when(pesagemService.getPesagensAnimaisAtivosByLoteId(LOTE_ID)).thenReturn(List.of(
                pesagem(analisado, 370, INICIO),
                pesagem(analisado, 400, INICIO.plusDays(30)),
                pesagem(rapido, 300, INICIO),
                pesagem(rapido, 360, INICIO.plusDays(30))));

        assertThrows(DadosInsuficientesException.class, () -> montar(produtor));
    }

    // ---------- Auxiliares ----------

    private ContextoAnaliseComparativaDTO montar(Usuario usuario) {
        return contextoComparativoService.montarComparativo(ANIMAL_ID, usuario);
    }

    private void loteCompleto() {
        Animal rapido = animal(2L, 202, 360);
        Animal lento = animal(3L, 303, 315);
        Animal novato = animal(4L, 404, 280);

        when(pesagemService.getPesagensAnimaisAtivosByLoteId(LOTE_ID)).thenReturn(List.of(
                pesagem(analisado, 370, INICIO),
                pesagem(analisado, 400, INICIO.plusDays(30)),
                pesagem(rapido, 300, INICIO),
                pesagem(rapido, 360, INICIO.plusDays(30)),
                pesagem(lento, 300, INICIO),
                pesagem(lento, 315, INICIO.plusDays(30)),
                pesagem(novato, 280, INICIO)));
    }

    private ContextoAnaliseAnimalDTO contextoDoAnimal(Double gmd) {
        return new ContextoAnaliseAnimalDTO(
                HOJE,
                null,
                new DesempenhoContextoDTO(gmd, 30L, 1.2, null),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                null);
    }

    private Usuario usuario(Perfil perfil) {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getPerfil()).thenReturn(perfil);
        return usuario;
    }

    private Animal animal(Long id, int brinco, double pesoAtual) {
        Animal animal = mock(Animal.class);
        lenient().when(animal.getId()).thenReturn(id);
        lenient().when(animal.getBrinco()).thenReturn(brinco);
        lenient().when(animal.getPesoAtual()).thenReturn(pesoAtual);
        return animal;
    }

    private Pesagem pesagem(Animal animal, double pesoKg, LocalDate data) {
        Pesagem pesagem = new Pesagem();
        pesagem.setAnimal(animal);
        pesagem.setPesoKg(pesoKg);
        pesagem.setData(data);
        return pesagem;
    }

    private Custo custo(double valor) {
        Custo custo = new Custo();
        custo.setValor(valor);
        return custo;
    }
}