package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.model.*;
import com.pecucore.system.core.repository.AlertaRepository;
import com.pecucore.system.core.repository.PesagemRepository;
import com.pecucore.system.core.repository.RegraAlertaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaPesoServiceTest {

    private static final Long ANIMAL_ID = 2L;
    private static final Long PROPRIEDADE_ID = 1L;

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private RegraAlertaRepository regraAlertaRepository;

    @Mock
    private PesagemRepository pesagemRepository;

    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private AlertaPesoService alertaPesoService;

    private Animal animal;

    @BeforeEach
    void setUp() {
        animal = mock(Animal.class);
        Lote lote = mock(Lote.class);
        Propriedade propriedade = mock(Propriedade.class);

        lenient().when(animal.getId()).thenReturn(ANIMAL_ID);
        lenient().when(animal.getLote()).thenReturn(lote);
        lenient().when(lote.getPropriedade()).thenReturn(propriedade);
        lenient().when(propriedade.getId()).thenReturn(PROPRIEDADE_ID);
    }

    // ---------- GMD abaixo da meta ----------

    @Test
    void deveCriarAlertaQuandoGmdFicaAbaixoDaRegra() {
        regraAtiva(TipoAlerta.GMD_ABAIXO_META, 1.0);
        alertaJaExiste(TipoAlerta.GMD_ABAIXO_META, false);

        alertaPesoService.verificarGmdAbaixoMeta(animal, 0.5);

        verify(alertaService).createAlerta(
                eq(animal), eq(TipoAlerta.GMD_ABAIXO_META), contains("0.5"));
    }

    @Test
    void naoDeveDuplicarAlertaDeGmdAtivo() {
        regraAtiva(TipoAlerta.GMD_ABAIXO_META, 1.0);
        alertaJaExiste(TipoAlerta.GMD_ABAIXO_META, true);

        alertaPesoService.verificarGmdAbaixoMeta(animal, 0.5);

        verify(alertaService, never()).createAlerta(any(), any(), anyString());
    }

    @Test
    void naoDeveAlertarQuandoGmdIgualAoLimiteDaRegra() {
        regraAtiva(TipoAlerta.GMD_ABAIXO_META, 1.0);

        alertaPesoService.verificarGmdAbaixoMeta(animal, 1.0);

        verifyNoInteractions(alertaService);
    }

    @Test
    void naoDeveAlertarGmdSemRegraAtiva() {
        when(regraAlertaRepository.findByPropriedadeIdAndTipoAndAtivaTrue(
                PROPRIEDADE_ID, TipoAlerta.GMD_ABAIXO_META))
                .thenReturn(List.of());

        alertaPesoService.verificarGmdAbaixoMeta(animal, 0.5);

        verifyNoInteractions(alertaService);
    }

    @Test
    void naoDeveFazerNadaQuandoGmdNulo() {
        alertaPesoService.verificarGmdAbaixoMeta(animal, null);

        verifyNoInteractions(regraAlertaRepository, alertaRepository, alertaService);
    }

    // ---------- Queda de peso ----------

    @Test
    void deveAlertarQuedaDePesoEmDuasPesagensSeguidas() {
        LocalDate hoje = LocalDate.now();
        historicoDePesagens(
                pesagem(300, hoje.minusDays(20)),
                pesagem(295, hoje.minusDays(10)),
                pesagem(290, hoje));
        alertaJaExiste(TipoAlerta.QUEDA_PESO, false);

        alertaPesoService.verificarQuedaPeso(animal);

        verify(alertaService).createAlerta(
                eq(animal), eq(TipoAlerta.QUEDA_PESO), anyString());
    }

    @Test
    void naoDeveAlertarQuedaDePesoComApenasUmaQueda() {
        LocalDate hoje = LocalDate.now();
        historicoDePesagens(
                pesagem(300, hoje.minusDays(20)),
                pesagem(295, hoje.minusDays(10)),
                pesagem(298, hoje));

        alertaPesoService.verificarQuedaPeso(animal);

        verifyNoInteractions(alertaService);
    }

    @Test
    void naoDeveAlertarQuedaDePesoQuandoPesoSeMantem() {
        LocalDate hoje = LocalDate.now();
        historicoDePesagens(
                pesagem(300, hoje.minusDays(20)),
                pesagem(300, hoje.minusDays(10)),
                pesagem(300, hoje));

        alertaPesoService.verificarQuedaPeso(animal);

        verifyNoInteractions(alertaService);
    }

    @Test
    void naoDeveAlertarQuedaDePesoComMenosDeTresPesagens() {
        LocalDate hoje = LocalDate.now();
        historicoDePesagens(
                pesagem(300, hoje.minusDays(10)),
                pesagem(290, hoje));

        alertaPesoService.verificarQuedaPeso(animal);

        verifyNoInteractions(alertaService);
    }

    @Test
    void naoDeveDuplicarAlertaDeQuedaDePeso() {
        LocalDate hoje = LocalDate.now();
        historicoDePesagens(
                pesagem(300, hoje.minusDays(20)),
                pesagem(295, hoje.minusDays(10)),
                pesagem(290, hoje));
        alertaJaExiste(TipoAlerta.QUEDA_PESO, true);

        alertaPesoService.verificarQuedaPeso(animal);

        verifyNoInteractions(alertaService);
    }

    // ---------- Pesagem atrasada ----------

    @Test
    void deveAlertarPesagemAtrasadaQuandoPassaDoLimiteDaRegra() {
        historicoDePesagens(pesagem(300, LocalDate.now().minusDays(31)));
        regraAtiva(TipoAlerta.PESAGEM_ATRASADA, 30);
        alertaJaExiste(TipoAlerta.PESAGEM_ATRASADA, false);

        alertaPesoService.verificarPesagemAtrasada(animal);

        verify(alertaService).createAlerta(
                eq(animal), eq(TipoAlerta.PESAGEM_ATRASADA), contains("31 dias"));
    }

    @Test
    void naoDeveAlertarPesagemNoLimiteExatoDaRegra() {
        historicoDePesagens(pesagem(300, LocalDate.now().minusDays(30)));
        regraAtiva(TipoAlerta.PESAGEM_ATRASADA, 30);

        alertaPesoService.verificarPesagemAtrasada(animal);

        verifyNoInteractions(alertaService);
    }

    @Test
    void naoDeveAlertarPesagemAtrasadaQuandoAnimalNaoTemPesagens() {
        historicoDePesagens();

        alertaPesoService.verificarPesagemAtrasada(animal);

        verifyNoInteractions(regraAlertaRepository, alertaService);
    }

    @Test
    void naoDeveDuplicarAlertaDePesagemAtrasada() {
        historicoDePesagens(pesagem(300, LocalDate.now().minusDays(31)));
        regraAtiva(TipoAlerta.PESAGEM_ATRASADA, 30);
        alertaJaExiste(TipoAlerta.PESAGEM_ATRASADA, true);

        alertaPesoService.verificarPesagemAtrasada(animal);

        verifyNoInteractions(alertaService);
    }

    // ---------- Auxiliares ----------

    private void regraAtiva(TipoAlerta tipo, double parametro) {
        RegraAlerta regra = new RegraAlerta();
        regra.setTipo(tipo);
        regra.setParametro(parametro);
        regra.setAtiva(true);

        when(regraAlertaRepository.findByPropriedadeIdAndTipoAndAtivaTrue(
                PROPRIEDADE_ID, tipo))
                .thenReturn(List.of(regra));
    }

    private void alertaJaExiste(TipoAlerta tipo, boolean existe) {
        when(alertaRepository.existsByAnimalIdAndTipoAndStatusIn(
                ANIMAL_ID, tipo, List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE)))
                .thenReturn(existe);
    }

    private void historicoDePesagens(Pesagem... pesagens) {
        when(pesagemRepository.findByAnimalIdOrderByDataAscIdAsc(ANIMAL_ID))
                .thenReturn(List.of(pesagens));
    }

    private Pesagem pesagem(double pesoKg, LocalDate data) {
        Pesagem pesagem = new Pesagem();
        pesagem.setPesoKg(pesoKg);
        pesagem.setData(data);
        return pesagem;
    }
}