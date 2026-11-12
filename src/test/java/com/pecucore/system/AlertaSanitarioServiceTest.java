package com.pecucore.system;

import com.pecucore.system.core.model.*;
import com.pecucore.system.core.repository.AlertaRepository;
import com.pecucore.system.core.repository.RegraAlertaRepository;
import com.pecucore.system.core.repository.VacinacaoRepository;
import com.pecucore.system.core.service.alerta.AlertaSanitarioService;
import com.pecucore.system.core.service.alerta.AlertaService;
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
class AlertaSanitarioServiceTest {

    private static final Long ANIMAL_ID = 2L;
    private static final Long PROPRIEDADE_ID = 1L;

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private RegraAlertaRepository regraAlertaRepository;

    @Mock
    private VacinacaoRepository vacinacaoRepository;

    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private AlertaSanitarioService alertaSanitarioService;

    private Animal animal;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        hoje = LocalDate.now();

        animal = mock(Animal.class);
        Lote lote = mock(Lote.class);
        Propriedade propriedade = mock(Propriedade.class);

        lenient().when(animal.getId()).thenReturn(ANIMAL_ID);
        lenient().when(animal.getLote()).thenReturn(lote);
        lenient().when(lote.getPropriedade()).thenReturn(propriedade);
        lenient().when(propriedade.getId()).thenReturn(PROPRIEDADE_ID);
    }

    // ---------- Vacina próxima do vencimento ----------

    @Test
    void deveAlertarVacinaQueVenceDentroDoPrazoDaRegra() {
        vacinacoesDoAnimal(vacinacao(hoje.plusDays(5), null));
        regraAtiva(TipoAlerta.VACINA_PROX_VENC, 7);
        alertaJaExiste(TipoAlerta.VACINA_PROX_VENC, false);

        alertaSanitarioService.verificarVacinaProximaVencimento(animal);

        verify(alertaService).criarAlerta(
                eq(animal), eq(TipoAlerta.VACINA_PROX_VENC), anyString());
    }

    @Test
    void deveAlertarVacinaQueVenceHoje() {
        vacinacoesDoAnimal(vacinacao(hoje, null));
        regraAtiva(TipoAlerta.VACINA_PROX_VENC, 7);
        alertaJaExiste(TipoAlerta.VACINA_PROX_VENC, false);

        alertaSanitarioService.verificarVacinaProximaVencimento(animal);

        verify(alertaService).criarAlerta(
                eq(animal), eq(TipoAlerta.VACINA_PROX_VENC), anyString());
    }

    @Test
    void deveAlertarVacinaQueVenceNoLimiteExatoDaRegra() {
        vacinacoesDoAnimal(vacinacao(hoje.plusDays(7), null));
        regraAtiva(TipoAlerta.VACINA_PROX_VENC, 7);
        alertaJaExiste(TipoAlerta.VACINA_PROX_VENC, false);

        alertaSanitarioService.verificarVacinaProximaVencimento(animal);

        verify(alertaService).criarAlerta(
                eq(animal), eq(TipoAlerta.VACINA_PROX_VENC), anyString());
    }

    @Test
    void naoDeveAlertarVacinaQueVenceForaDoPrazoDaRegra() {
        vacinacoesDoAnimal(vacinacao(hoje.plusDays(8), null));
        regraAtiva(TipoAlerta.VACINA_PROX_VENC, 7);

        alertaSanitarioService.verificarVacinaProximaVencimento(animal);

        verifyNoInteractions(alertaService);
    }

    @Test
    void naoDeveAlertarComoProximaUmaVacinaQueJaVenceu() {
        vacinacoesDoAnimal(vacinacao(hoje.minusDays(1), null));
        regraAtiva(TipoAlerta.VACINA_PROX_VENC, 7);

        alertaSanitarioService.verificarVacinaProximaVencimento(animal);

        verifyNoInteractions(alertaService);
    }

    @Test
    void deveCriarUmUnicoAlertaMesmoComVariasVacinasNoPrazo() {
        vacinacoesDoAnimal(
                vacinacao(hoje.plusDays(2), null),
                vacinacao(hoje.plusDays(3), null));
        regraAtiva(TipoAlerta.VACINA_PROX_VENC, 7);
        alertaJaExiste(TipoAlerta.VACINA_PROX_VENC, false);

        alertaSanitarioService.verificarVacinaProximaVencimento(animal);

        verify(alertaService, times(1)).criarAlerta(
                eq(animal), eq(TipoAlerta.VACINA_PROX_VENC), anyString());
    }

    @Test
    void naoDeveDuplicarAlertaDeVacinaProxima() {
        vacinacoesDoAnimal(vacinacao(hoje.plusDays(5), null));
        regraAtiva(TipoAlerta.VACINA_PROX_VENC, 7);
        alertaJaExiste(TipoAlerta.VACINA_PROX_VENC, true);

        alertaSanitarioService.verificarVacinaProximaVencimento(animal);

        verifyNoInteractions(alertaService);
    }

    @Test
    void naoDeveAlertarVacinaProximaSemRegraAtiva() {
        vacinacoesDoAnimal(vacinacao(hoje.plusDays(5), null));
        when(regraAlertaRepository.findByPropriedadeIdAndTipoAndAtivaTrue(
                PROPRIEDADE_ID, TipoAlerta.VACINA_PROX_VENC))
                .thenReturn(List.of());

        alertaSanitarioService.verificarVacinaProximaVencimento(animal);

        verifyNoInteractions(alertaService);
    }

    // ---------- Vacina vencida ----------

    @Test
    void deveAlertarVacinaVencida() {
        vacinacoesDoAnimal(vacinacao(hoje.minusDays(1), null));
        alertaJaExiste(TipoAlerta.VACINA_VENC, false);

        alertaSanitarioService.verificarVacinaVencida(animal);

        verify(alertaService).criarAlerta(
                eq(animal), eq(TipoAlerta.VACINA_VENC), anyString());
    }

    @Test
    void naoDeveConsiderarVencidaVacinaComDoseParaHoje() {
        vacinacoesDoAnimal(vacinacao(hoje, null));

        alertaSanitarioService.verificarVacinaVencida(animal);

        verifyNoInteractions(alertaService);
    }

    @Test
    void naoDeveDuplicarAlertaDeVacinaVencida() {
        vacinacoesDoAnimal(vacinacao(hoje.minusDays(1), null));
        alertaJaExiste(TipoAlerta.VACINA_VENC, true);

        alertaSanitarioService.verificarVacinaVencida(animal);

        verifyNoInteractions(alertaService);
    }

    @Test
    void deveIgnorarVacinaSemProximaDose() {
        vacinacoesDoAnimal(vacinacao(null, null));

        alertaSanitarioService.verificarVacinaVencida(animal);

        verifyNoInteractions(alertaService);
    }

    // ---------- Período de carência ----------

    @Test
    void deveAlertarAnimalEmPeriodoDeCarencia() {
        vacinacoesDoAnimal(vacinacao(null, hoje.plusDays(3)));
        alertaJaExiste(TipoAlerta.PERIODO_CARENCIA, false);

        alertaSanitarioService.verificarPeriodoCarencia(animal);

        verify(alertaService).criarAlerta(
                eq(animal), eq(TipoAlerta.PERIODO_CARENCIA), anyString());
    }

    @Test
    void naoDeveAlertarCarenciaNoUltimoDia() {
        vacinacoesDoAnimal(vacinacao(null, hoje));

        alertaSanitarioService.verificarPeriodoCarencia(animal);

        verifyNoInteractions(alertaService);
    }

    @Test
    void naoDeveDuplicarAlertaDeCarencia() {
        vacinacoesDoAnimal(vacinacao(null, hoje.plusDays(3)));
        alertaJaExiste(TipoAlerta.PERIODO_CARENCIA, true);

        alertaSanitarioService.verificarPeriodoCarencia(animal);

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

    private void vacinacoesDoAnimal(Vacinacao... vacinacoes) {
        when(vacinacaoRepository.findByAnimalIdOrderByDataAscIdAsc(ANIMAL_ID))
                .thenReturn(List.of(vacinacoes));
    }

    private Vacinacao vacinacao(LocalDate dataProximaDose, LocalDate dataFimCarencia) {
        Vacinacao vacinacao = new Vacinacao();
        vacinacao.setDataProximaDose(dataProximaDose);
        vacinacao.setDataFimCarencia(dataFimCarencia);
        return vacinacao;
    }
}