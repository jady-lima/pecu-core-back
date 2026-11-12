package com.pecucore.system;

import com.pecucore.system.bovinos.agrupamento.MetaDesempenhoService;
import com.pecucore.system.bovinos.desempenho.GmdService;
import com.pecucore.system.bovinos.sanidade.CarenciaService;
import com.pecucore.system.core.dto.analise.ContextoAnaliseAnimalDTO;
import com.pecucore.system.core.dto.analise.DesempenhoContextoDTO;
import com.pecucore.system.core.dto.analise.ResumoCustoDTO;
import com.pecucore.system.core.exception.DadosInsuficientesException;
import com.pecucore.system.core.model.*;
import com.pecucore.system.core.repository.AlertaRepository;
import com.pecucore.system.core.service.AlimentacaoService;
import com.pecucore.system.core.service.AnimalService;
import com.pecucore.system.core.service.MedicamentoService;
import com.pecucore.system.core.service.VacinacaoService;
import com.pecucore.system.core.service.analise.ContextoAnaliseService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextoAnaliseServiceTest {

    private static final Long ANIMAL_ID = 1L;
    private static final LocalDate INICIO = LocalDate.of(2026, 9, 1);
    private static final double PRECISAO = 0.001;

    @Mock
    private AnimalService animalService;

    @Mock
    private PesagemService pesagemService;

    @Mock
    private AlimentacaoService alimentacaoService;

    @Mock
    private VacinacaoService vacinacaoService;

    @Mock
    private MedicamentoService medicamentoService;

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private CustoService custoService;

    @Mock
    private MetaDesempenhoService metaDesempenhoService;

    @Mock
    private CarenciaService carenciaService;

    @Spy
    private GmdService gmdService = new GmdService();

    @Spy
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira =
            new PoliticaVisibilidadeFinanceira();

    @InjectMocks
    private ContextoAnaliseService contextoAnaliseService;

    private Animal animal;
    private Lote lote;
    private Usuario produtor;
    private Usuario veterinario;

    @BeforeEach
    void setUp() {
        produtor = usuario(Perfil.PRODUTOR);
        veterinario = usuario(Perfil.VETERINARIO);

        lote = mock(Lote.class);

        animal = mock(Animal.class);
        lenient().when(animal.getId()).thenReturn(ANIMAL_ID);
        lenient().when(animal.getLote()).thenReturn(lote);

        lenient().when(animalService.getAnimalById(eq(ANIMAL_ID), any(Usuario.class)))
                .thenReturn(animal);
    }

    // ---------- Dados mínimos ----------

    @Test
    void deveExigirPeloMenosDuasPesagens() {
        historicoDePesagens(pesagem(300, INICIO));

        assertThrows(DadosInsuficientesException.class, () -> montar(produtor));

        verifyNoInteractions(alimentacaoService, vacinacaoService, medicamentoService,
                carenciaService, alertaRepository, custoService);
    }

    @Test
    void dataDeReferenciaEHoje() {
        historicoDePesagens(pesagem(300, INICIO), pesagem(330, INICIO.plusDays(30)));

        assertEquals(LocalDate.now(), montar(produtor).dataReferencia());
    }

    // ---------- Desempenho (R1 e R3) ----------

    @Test
    void deveMontarDesempenhoComGmdEMetaDoLote() {
        historicoDePesagens(pesagem(300, INICIO), pesagem(330, INICIO.plusDays(30)));
        when(metaDesempenhoService.getGmdEsperado(lote)).thenReturn(Optional.of(1.5));

        DesempenhoContextoDTO desempenho = montar(produtor).desempenho();

        assertEquals(1.0, desempenho.gmdPeriodoKgDia(), PRECISAO);
        assertEquals(30L, desempenho.diasAvaliados());
        assertEquals(1.5, desempenho.gmdMetaKgDia(), PRECISAO);
        assertEquals(66.667, desempenho.percentualDaMeta(), PRECISAO);
    }

    @Test
    void semMetaODesempenhoFicaSemPercentual() {
        historicoDePesagens(pesagem(300, INICIO), pesagem(330, INICIO.plusDays(30)));
        when(metaDesempenhoService.getGmdEsperado(lote)).thenReturn(Optional.empty());

        DesempenhoContextoDTO desempenho = montar(produtor).desempenho();

        assertEquals(1.0, desempenho.gmdPeriodoKgDia(), PRECISAO);
        assertNull(desempenho.gmdMetaKgDia());
        assertNull(desempenho.percentualDaMeta());
    }

    // ---------- Carência (R2) ----------

    @Test
    void deveInformarQueOAnimalEstaEmCarencia() {
        historicoDePesagens(pesagem(300, INICIO), pesagem(330, INICIO.plusDays(30)));
        when(carenciaService.estaEmCarencia(ANIMAL_ID)).thenReturn(true);

        assertTrue(montar(produtor).animal().emCarencia());
    }

    @Test
    void deveInformarQueOAnimalNaoEstaEmCarencia() {
        historicoDePesagens(pesagem(300, INICIO), pesagem(330, INICIO.plusDays(30)));
        when(carenciaService.estaEmCarencia(ANIMAL_ID)).thenReturn(false);

        assertFalse(montar(produtor).animal().emCarencia());
    }

    // ---------- Histórico ----------

    @Test
    void deveIncluirOHistoricoCompletoDoAnimal() {
        historicoDePesagens(pesagem(300, INICIO), pesagem(330, INICIO.plusDays(30)));
        when(alimentacaoService.getHistoricoByAnimalId(ANIMAL_ID)).thenReturn(List.of(alimentacao()));
        when(vacinacaoService.getHistoricoByAnimalId(ANIMAL_ID)).thenReturn(List.of(vacinacao()));
        when(medicamentoService.getHistoricoByAnimalId(ANIMAL_ID)).thenReturn(List.of(medicamento()));

        ContextoAnaliseAnimalDTO contexto = montar(produtor);

        assertEquals(2, contexto.pesagens().size());
        assertEquals(1, contexto.alimentacoes().size());
        assertEquals("Aftosa", contexto.vacinacoes().get(0).vacina());
        assertEquals(INICIO.plusDays(21), contexto.vacinacoes().get(0).dataFimCarencia());
        assertEquals("Ivermectina", contexto.medicamentos().get(0).nome());
        assertEquals(INICIO.plusDays(10), contexto.medicamentos().get(0).dataFimCarencia());
    }

    // ---------- Visibilidade financeira ----------

    @Test
    void produtorRecebeOResumoDeCustos() {
        historicoDePesagens(pesagem(300, INICIO), pesagem(330, INICIO.plusDays(30)));
        when(custoService.getByAnimalId(ANIMAL_ID)).thenReturn(List.of(
                custo(TipoCusto.ALIMENTACAO, 100.0),
                custo(TipoCusto.VACINA, 50.0)));

        ResumoCustoDTO custos = montar(produtor).custos();

        assertEquals(150.0, custos.total(), PRECISAO);
        assertEquals(100.0, custos.totalPorTipo().get(TipoCusto.ALIMENTACAO), PRECISAO);
        assertEquals(50.0, custos.totalPorTipo().get(TipoCusto.VACINA), PRECISAO);
    }

    @Test
    void veterinarioNaoRecebeCustos() {
        historicoDePesagens(pesagem(300, INICIO), pesagem(330, INICIO.plusDays(30)));

        assertNull(montar(veterinario).custos());

        verifyNoInteractions(custoService);
    }

    @Test
    void veterinarioNaoRecebeAlertasFinanceiros() {
        historicoDePesagens(pesagem(300, INICIO), pesagem(330, INICIO.plusDays(30)));
        alertasDoAnimal(TipoAlerta.QUEDA_PESO, TipoAlerta.CUSTO_ACIMA);

        ContextoAnaliseAnimalDTO contexto = montar(veterinario);

        assertEquals(1, contexto.alertas().size());
        assertEquals(TipoAlerta.QUEDA_PESO, contexto.alertas().get(0).tipo());
    }

    @Test
    void produtorRecebeTodosOsAlertas() {
        historicoDePesagens(pesagem(300, INICIO), pesagem(330, INICIO.plusDays(30)));
        alertasDoAnimal(TipoAlerta.QUEDA_PESO, TipoAlerta.CUSTO_ACIMA);

        assertEquals(2, montar(produtor).alertas().size());
    }

    // ---------- Auxiliares ----------

    private ContextoAnaliseAnimalDTO montar(Usuario usuario) {
        return contextoAnaliseService.montarIndividual(ANIMAL_ID, usuario);
    }

    private Usuario usuario(Perfil perfil) {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getPerfil()).thenReturn(perfil);
        return usuario;
    }

    private void historicoDePesagens(Pesagem... pesagens) {
        when(pesagemService.getHistoricoByAnimalId(ANIMAL_ID)).thenReturn(List.of(pesagens));
    }

    private Pesagem pesagem(double pesoKg, LocalDate data) {
        Pesagem pesagem = new Pesagem();
        pesagem.setPesoKg(pesoKg);
        pesagem.setData(data);
        return pesagem;
    }

    private Alimentacao alimentacao() {
        Alimentacao alimentacao = new Alimentacao();
        alimentacao.setTipo(TipoAlimentacao.RACAO);
        alimentacao.setQuantidade(12.5);
        alimentacao.setData(INICIO);
        return alimentacao;
    }

    private Vacinacao vacinacao() {
        Vacina vacina = new Vacina();
        vacina.setNome("Aftosa");

        Vacinacao vacinacao = new Vacinacao();
        vacinacao.setVacina(vacina);
        vacinacao.setDoseAplicada(2.0);
        vacinacao.setData(INICIO);
        vacinacao.setDataFimCarencia(INICIO.plusDays(21));
        return vacinacao;
    }

    private Medicamento medicamento() {
        Medicamento medicamento = new Medicamento();
        medicamento.setNome("Ivermectina");
        medicamento.setMotivo("Vermifugação");
        medicamento.setDosagem(5.0);
        medicamento.setData(INICIO);
        medicamento.setDataFimCarencia(INICIO.plusDays(10));
        return medicamento;
    }

    private Custo custo(TipoCusto tipo, double valor) {
        Custo custo = new Custo();
        custo.setTipo(tipo);
        custo.setValor(valor);
        return custo;
    }

    private void alertasDoAnimal(TipoAlerta... tipos) {
        List<Alerta> alertas = java.util.Arrays.stream(tipos)
                .map(tipo -> {
                    Alerta alerta = new Alerta();
                    alerta.setTipo(tipo);
                    return alerta;
                })
                .toList();

        when(alertaRepository.findByAnimalIdOrderByDataCriacaoDesc(ANIMAL_ID)).thenReturn(alertas);
    }
}