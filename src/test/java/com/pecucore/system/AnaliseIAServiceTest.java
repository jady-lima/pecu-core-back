package com.pecucore.system;

import com.pecucore.system.dto.analise.*;
import com.pecucore.system.exception.AcessoNegadoException;
import com.pecucore.system.exception.ServicoIAIndisponivelException;
import com.pecucore.system.ia.AnaliseAnimalIAFacade;
import com.pecucore.system.ia.ResultadoAnaliseIA;
import com.pecucore.system.model.*;
import com.pecucore.system.repository.AnaliseIARepository;
import com.pecucore.system.service.AnimalService;
import com.pecucore.system.service.UsuarioService;
import com.pecucore.system.service.analise.AnaliseIAService;
import com.pecucore.system.service.analise.ContextoAnaliseService;
import com.pecucore.system.service.analise.ContextoComparativoService;
import com.pecucore.system.service.politica.PoliticaVisibilidadeFinanceira;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnaliseIAServiceTest {

    private static final Long ANIMAL_ID = 2L;
    private static final Long PRODUTOR_ID = 100L;
    private static final Long ANALISE_ID = 50L;
    private static final String CONTEUDO_IA = "{\"situacaoGeral\":\"ESTAVEL\"}";
    private static final String MODELO = "gemini-simulado";

    @Mock
    private AnaliseIARepository analiseIARepository;

    @Mock
    private AnimalService animalService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ContextoAnaliseService contextoAnaliseService;

    @Mock
    private ContextoComparativoService contextoComparativoService;

    @Mock
    private AnaliseAnimalIAFacade analiseAnimalIAFacade;

    @Spy
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira =
            new PoliticaVisibilidadeFinanceira();

    @InjectMocks
    private AnaliseIAService analiseIAService;

    private Animal animal;
    private Usuario produtor;
    private Usuario veterinario;

    @BeforeEach
    void setUp() {
        animal = mock(Animal.class);
        lenient().when(animal.getId()).thenReturn(ANIMAL_ID);

        produtor = usuario(PRODUTOR_ID, Perfil.PRODUTOR);
        veterinario = usuario(200L, Perfil.VETERINARIO);
    }

    // ---------- Geração e gravação no histórico ----------

    @Test
    void analiseIndividualDeveSerGuardadaNoHistorico() {
        ContextoAnaliseAnimalDTO contexto = contextoIndividual(new ResumoCustoDTO(350.0, Map.of()));
        when(contextoAnaliseService.montarIndividual(ANIMAL_ID, produtor)).thenReturn(contexto);
        when(analiseAnimalIAFacade.analisarIndividual(contexto)).thenReturn(resultadoDaIA());
        prepararGravacao();

        AnaliseIA analise = analiseIAService.gerarIndividual(ANIMAL_ID, produtor);

        assertEquals(TipoAnalise.INDIVIDUAL, analise.getTipo());
        assertSame(animal, analise.getAnimal());
        assertSame(produtor, analise.getSolicitante());
        assertEquals(CONTEUDO_IA, analise.getConteudo());
        assertEquals(MODELO, analise.getModeloUtilizado());
        assertTrue(analise.isIncluiDadosFinanceiros());

        verify(analiseIARepository).save(analise);
    }

    @Test
    void analiseIndividualSemCustosNaoMarcaDadosFinanceiros() {
        ContextoAnaliseAnimalDTO contexto = contextoIndividual(null);
        when(contextoAnaliseService.montarIndividual(ANIMAL_ID, produtor)).thenReturn(contexto);
        when(analiseAnimalIAFacade.analisarIndividual(contexto)).thenReturn(resultadoDaIA());
        prepararGravacao();

        AnaliseIA analise = analiseIAService.gerarIndividual(ANIMAL_ID, produtor);

        assertFalse(analise.isIncluiDadosFinanceiros());
    }

    @Test
    void analiseComparativaMarcaDadosFinanceirosQuandoLoteTemCustoMedio() {
        ContextoAnaliseComparativaDTO contexto = contextoComparativo(120.0);
        when(contextoComparativoService.montarComparativo(ANIMAL_ID, produtor)).thenReturn(contexto);
        when(analiseAnimalIAFacade.analisarComparativo(contexto)).thenReturn(resultadoDaIA());
        prepararGravacao();

        AnaliseIA analise = analiseIAService.gerarComparativa(ANIMAL_ID, produtor);

        assertEquals(TipoAnalise.COMPARATIVA, analise.getTipo());
        assertTrue(analise.isIncluiDadosFinanceiros());
    }

    @Test
    void analiseComparativaSemCustosNaoMarcaDadosFinanceiros() {
        ContextoAnaliseComparativaDTO contexto = contextoComparativo(null);
        when(contextoComparativoService.montarComparativo(ANIMAL_ID, produtor)).thenReturn(contexto);
        when(analiseAnimalIAFacade.analisarComparativo(contexto)).thenReturn(resultadoDaIA());
        prepararGravacao();

        AnaliseIA analise = analiseIAService.gerarComparativa(ANIMAL_ID, produtor);

        assertFalse(analise.isIncluiDadosFinanceiros());
    }

    @Test
    void naoDeveGuardarHistoricoQuandoIAFalha() {
        ContextoAnaliseAnimalDTO contexto = contextoIndividual(null);
        when(contextoAnaliseService.montarIndividual(ANIMAL_ID, produtor)).thenReturn(contexto);
        when(analiseAnimalIAFacade.analisarIndividual(contexto))
                .thenThrow(new ServicoIAIndisponivelException("Serviço indisponível"));

        assertThrows(ServicoIAIndisponivelException.class,
                () -> analiseIAService.gerarIndividual(ANIMAL_ID, produtor));

        verifyNoInteractions(analiseIARepository);
    }

    // ---------- Visibilidade do histórico ----------

    @Test
    void historicoDoVeterinarioOcultaAnalisesComDadosFinanceiros() {
        AnaliseIA semDadosFinanceiros = analise(false);
        AnaliseIA comDadosFinanceiros = analise(true);
        when(analiseIARepository.findByAnimalIdOrderByDataGeracaoDesc(ANIMAL_ID))
                .thenReturn(List.of(semDadosFinanceiros, comDadosFinanceiros));

        List<AnaliseIA> historico = analiseIAService.getHistoricoByAnimal(ANIMAL_ID, veterinario);

        assertEquals(List.of(semDadosFinanceiros), historico);
        verify(animalService).getAnimalById(ANIMAL_ID, veterinario);
    }

    @Test
    void historicoDoProdutorMostraTodasAsAnalises() {
        AnaliseIA semDadosFinanceiros = analise(false);
        AnaliseIA comDadosFinanceiros = analise(true);
        when(analiseIARepository.findByAnimalIdOrderByDataGeracaoDesc(ANIMAL_ID))
                .thenReturn(List.of(semDadosFinanceiros, comDadosFinanceiros));

        List<AnaliseIA> historico = analiseIAService.getHistoricoByAnimal(ANIMAL_ID, produtor);

        assertEquals(List.of(semDadosFinanceiros, comDadosFinanceiros), historico);
    }

    @Test
    void veterinarioNaoPodeAbrirAnaliseComDadosFinanceiros() {
        when(analiseIARepository.findById(ANALISE_ID)).thenReturn(Optional.of(analise(true)));

        assertThrows(AcessoNegadoException.class,
                () -> analiseIAService.getAnaliseById(ANALISE_ID, veterinario));
    }

    // ---------- Auxiliares ----------

    private Usuario usuario(Long id, Perfil perfil) {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(id);
        lenient().when(usuario.getPerfil()).thenReturn(perfil);
        return usuario;
    }

    private void prepararGravacao() {
        when(animalService.getAnimalById(ANIMAL_ID, produtor)).thenReturn(animal);
        when(usuarioService.getUsuarioById(PRODUTOR_ID)).thenReturn(produtor);
        when(analiseIARepository.save(any(AnaliseIA.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private ContextoAnaliseAnimalDTO contextoIndividual(ResumoCustoDTO custos) {
        return new ContextoAnaliseAnimalDTO(
                LocalDate.now(), null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(),
                custos);
    }

    private ContextoAnaliseComparativaDTO contextoComparativo(Double custoMedioPorAnimal) {
        EstatisticasLoteDTO estatisticas = new EstatisticasLoteDTO(
                10, 10, null, null, null, null, null, 0.0, custoMedioPorAnimal);

        return new ContextoAnaliseComparativaDTO(
                LocalDate.now(), contextoIndividual(null), null, estatisticas, List.of());
    }

    private ResultadoAnaliseIA resultadoDaIA() {
        RelatorioAnaliseDTO relatorio = new RelatorioAnaliseDTO(
                SituacaoGeralAnimal.ESTAVEL, "Animal estável", "Desempenho adequado",
                List.of(), List.of(), List.of(), List.of(), List.of());

        return new ResultadoAnaliseIA(relatorio, CONTEUDO_IA, MODELO);
    }

    private AnaliseIA analise(boolean incluiDadosFinanceiros) {
        AnaliseIA analise = new AnaliseIA();
        analise.setAnimal(animal);
        analise.setIncluiDadosFinanceiros(incluiDadosFinanceiros);
        return analise;
    }
}