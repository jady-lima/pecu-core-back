package com.pecucore.system;

import com.pecucore.system.core.exception.AcessoNegadoException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.exception.RegraNegocioException;

import com.pecucore.system.core.model.*;
import com.pecucore.system.core.repository.AlertaRepository;
import com.pecucore.system.core.repository.CustoRepository;
import com.pecucore.system.core.repository.RegraAlertaRepository;
import com.pecucore.system.core.service.alerta.AlertaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    private static final Long ANIMAL_ID = 2L;
    private static final Long PROPRIEDADE_ID = 1L;
    private static final Long ALERTA_ID = 10L;

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private RegraAlertaRepository regraAlertaRepository;

    @Mock
    private CustoRepository custoRepository;

    @InjectMocks
    private AlertaService alertaService;

    private Animal animal;
    private Usuario produtor;
    private Usuario veterinario;

    @BeforeEach
    void setUp() {
        produtor = usuario(100L, Perfil.PRODUTOR);
        veterinario = usuario(200L, Perfil.VETERINARIO);

        Propriedade propriedade = mock(Propriedade.class);
        lenient().when(propriedade.getId()).thenReturn(PROPRIEDADE_ID);
        lenient().when(propriedade.getUsuario()).thenReturn(produtor);
        lenient().when(propriedade.getVeterinarios()).thenReturn(Set.of(veterinario));

        Lote lote = mock(Lote.class);
        lenient().when(lote.getPropriedade()).thenReturn(propriedade);

        animal = mock(Animal.class);
        lenient().when(animal.getId()).thenReturn(ANIMAL_ID);
        lenient().when(animal.getLote()).thenReturn(lote);
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    // ---------- Criação do alerta ----------

    @Test
    void deveCriarAlertaAbertoComDataDeCriacao() {
        alertaService.criarAlerta(animal, TipoAlerta.QUEDA_PESO, "Mensagem de teste");

        Alerta salvo = alertaSalvoNoRepositorio();

        assertSame(animal, salvo.getAnimal());
        assertEquals(TipoAlerta.QUEDA_PESO, salvo.getTipo());
        assertEquals("Mensagem de teste", salvo.getMensagem());
        assertEquals(StatusAlerta.ABERTO, salvo.getStatus());
        assertNotNull(salvo.getDataCriacao());
    }

    @ParameterizedTest(name = "{0} deve ter severidade {1}")
    @CsvSource({
            "QUEDA_PESO, ALTA",
            "VACINA_VENC, ALTA",
            "GMD_ABAIXO_META, ALTA",
            "CUSTO_ACIMA, ALTA",
            "VACINA_PROX_VENC, BAIXA",
            "PERIODO_CARENCIA, MEDIA",
            "PESAGEM_ATRASADA, MEDIA"
    })
    void deveDefinirSeveridadePeloTipo(TipoAlerta tipo, SeveridadeAlerta severidadeEsperada) {
        alertaService.criarAlerta(animal, tipo, "mensagem");

        assertEquals(severidadeEsperada, alertaSalvoNoRepositorio().getSeveridade());
    }

    // ---------- Custo acima do limite ----------

    @Test
    void deveAlertarQuandoCustoAcumuladoPassaDoLimite() {
        custosDoAnimal(300.0, 250.0);
        regraAtiva(TipoAlerta.CUSTO_ACIMA, 500.0);
        alertaJaExiste(TipoAlerta.CUSTO_ACIMA, false);

        alertaService.verificarCustoAcima(animal);

        Alerta salvo = alertaSalvoNoRepositorio();
        assertEquals(TipoAlerta.CUSTO_ACIMA, salvo.getTipo());
        assertTrue(salvo.getMensagem().contains("550.0"));
    }

    @Test
    void naoDeveAlertarCustoIgualAoLimite() {
        custosDoAnimal(300.0, 200.0);
        regraAtiva(TipoAlerta.CUSTO_ACIMA, 500.0);

        alertaService.verificarCustoAcima(animal);

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void naoDeveAlertarCustoQuandoAnimalNaoTemCustos() {
        custosDoAnimal();

        alertaService.verificarCustoAcima(animal);

        verifyNoInteractions(regraAlertaRepository, alertaRepository);
    }

    @Test
    void naoDeveDuplicarAlertaDeCusto() {
        custosDoAnimal(300.0, 250.0);
        regraAtiva(TipoAlerta.CUSTO_ACIMA, 500.0);
        alertaJaExiste(TipoAlerta.CUSTO_ACIMA, true);

        alertaService.verificarCustoAcima(animal);

        verify(alertaRepository, never()).save(any());
    }

    // ---------- Atualização de status e visibilidade ----------

    @Test
    void produtorDaPropriedadePodeColocarAlertaEmAnalise() {
        autenticar(produtor);
        alertaCadastrado(alerta(TipoAlerta.QUEDA_PESO));
        saveDevolveOProprioAlerta();

        Alerta atualizado = alertaService.atualizarStatus(ALERTA_ID, StatusAlerta.EM_ANALISE);

        assertEquals(StatusAlerta.EM_ANALISE, atualizado.getStatus());
    }

    @Test
    void veterinarioAssociadoPodeAtualizarAlertaZootecnico() {
        autenticar(veterinario);
        alertaCadastrado(alerta(TipoAlerta.VACINA_VENC));
        saveDevolveOProprioAlerta();

        Alerta atualizado = alertaService.atualizarStatus(ALERTA_ID, StatusAlerta.RESOLVIDO);

        assertEquals(StatusAlerta.RESOLVIDO, atualizado.getStatus());
    }

    @Test
    void veterinarioNaoPodeAtualizarAlertaFinanceiro() {
        autenticar(veterinario);
        alertaCadastrado(alerta(TipoAlerta.CUSTO_ACIMA));

        assertThrows(AcessoNegadoException.class,
                () -> alertaService.atualizarStatus(ALERTA_ID, StatusAlerta.RESOLVIDO));

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void produtorDeOutraPropriedadeNaoPodeAtualizarAlerta() {
        autenticar(usuario(300L, Perfil.PRODUTOR));
        alertaCadastrado(alerta(TipoAlerta.QUEDA_PESO));

        assertThrows(AcessoNegadoException.class,
                () -> alertaService.atualizarStatus(ALERTA_ID, StatusAlerta.RESOLVIDO));

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void adminPodeAtualizarQualquerAlerta() {
        autenticar(usuario(999L, Perfil.ADMIN));
        alertaCadastrado(alerta(TipoAlerta.CUSTO_ACIMA));
        saveDevolveOProprioAlerta();

        Alerta atualizado = alertaService.atualizarStatus(ALERTA_ID, StatusAlerta.IGNORADO);

        assertEquals(StatusAlerta.IGNORADO, atualizado.getStatus());
    }

    @Test
    void deveExigirNovoStatus() {
        assertThrows(RegraNegocioException.class,
                () -> alertaService.atualizarStatus(ALERTA_ID, null));

        verifyNoInteractions(alertaRepository);
    }

    @Test
    void deveFalharQuandoAlertaNaoExiste() {
        when(alertaRepository.findById(ALERTA_ID)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> alertaService.atualizarStatus(ALERTA_ID, StatusAlerta.EM_ANALISE));
    }

    @Test
    void listaDoVeterinarioNaoMostraAlertasFinanceiros() {
        autenticar(veterinario);
        Alerta queda = alerta(TipoAlerta.QUEDA_PESO);
        Alerta custo = alerta(TipoAlerta.CUSTO_ACIMA);
        when(alertaRepository.findAll()).thenReturn(List.of(queda, custo));

        List<Alerta> resultado = alertaService.listarAlertas();

        assertEquals(List.of(queda), resultado);
    }

    @Test
    void listaDoProdutorMostraTodosOsAlertasDaPropriedade() {
        autenticar(produtor);
        Alerta queda = alerta(TipoAlerta.QUEDA_PESO);
        Alerta custo = alerta(TipoAlerta.CUSTO_ACIMA);
        when(alertaRepository.findAll()).thenReturn(List.of(queda, custo));

        List<Alerta> resultado = alertaService.listarAlertas();

        assertEquals(List.of(queda, custo), resultado);
    }

    // ---------- Auxiliares ----------

    private Usuario usuario(Long id, Perfil perfil) {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(id);
        lenient().when(usuario.getPerfil()).thenReturn(perfil);
        return usuario;
    }

    private void autenticar(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    private Alerta alerta(TipoAlerta tipo) {
        Alerta alerta = new Alerta();
        alerta.setAnimal(animal);
        alerta.setTipo(tipo);
        alerta.setStatus(StatusAlerta.ABERTO);
        return alerta;
    }

    private void alertaCadastrado(Alerta alerta) {
        when(alertaRepository.findById(ALERTA_ID)).thenReturn(Optional.of(alerta));
    }

    private void saveDevolveOProprioAlerta() {
        when(alertaRepository.save(any(Alerta.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private Alerta alertaSalvoNoRepositorio() {
        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        return captor.getValue();
    }

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

    private void custosDoAnimal(double... valores) {
        List<Custo> custos = Arrays.stream(valores)
                .mapToObj(valor -> {
                    Custo custo = new Custo();
                    custo.setValor(valor);
                    return custo;
                })
                .toList();

        when(custoRepository.findByAnimalIdOrderByDataAscIdAsc(ANIMAL_ID))
                .thenReturn(custos);
    }
}