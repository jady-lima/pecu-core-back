package com.pecucore.system;

import com.pecucore.system.bovinos.sanidade.CalculadoraCarencia;
import com.pecucore.system.core.dto.vacinacao.VacinacaoRequestDTO;
import com.pecucore.system.core.dto.vacinacao.VacinacaoUpdateRequestDTO;
import com.pecucore.system.core.model.*;
import com.pecucore.system.core.repository.VacinacaoRepository;
import com.pecucore.system.core.service.AnimalService;
import com.pecucore.system.core.service.UsuarioService;
import com.pecucore.system.core.service.VacinaService;
import com.pecucore.system.core.service.VacinacaoService;
import com.pecucore.system.core.service.custo.CustoAutomaticoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacinacaoServiceTest {

    private static final int BRINCO = 123;
    private static final Long VACINA_ID = 7L;
    private static final Long APLICADOR_ID = 200L;
    private static final Long USUARIO_ID = 100L;
    private static final Long VACINACAO_ID = 30L;
    private static final LocalDate DATA = LocalDate.of(2026, 11, 1);

    @Mock
    private VacinacaoRepository vacinacaoRepository;

    @Mock
    private AnimalService animalService;

    @Mock
    private VacinaService vacinaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CustoAutomaticoService custoAutomaticoService;

    @Spy
    private CalculadoraCarencia calculadoraCarencia = new CalculadoraCarencia();

    @InjectMocks
    private VacinacaoService vacinacaoService;

    private Usuario usuario;
    private Usuario aplicador;
    private Animal animal;
    private Vacina vacina;

    @BeforeEach
    void setUp() {
        usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(USUARIO_ID);

        aplicador = mock(Usuario.class);

        animal = mock(Animal.class);
        lenient().when(animal.getId()).thenReturn(2L);

        vacina = new Vacina();
        vacina.setCarenciaDias(21);
        vacina.setIntervaloDoseDias(180);

        lenient().when(animalService.getAnimalAtivoByBrinco(BRINCO, usuario)).thenReturn(animal);
        lenient().when(vacinaService.getVacinaById(VACINA_ID)).thenReturn(vacina);
        lenient().when(usuarioService.getUsuarioById(APLICADOR_ID)).thenReturn(aplicador);
        lenient().when(usuarioService.getUsuarioById(USUARIO_ID)).thenReturn(usuario);
        lenient().when(vacinacaoRepository.save(any(Vacinacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    // ---------- Carência e próxima dose ----------

    @Test
    void deveCalcularFimDaCarenciaEProximaDose() {
        Vacinacao vacinacao = vacinacaoService.create(dados(45.0), usuario);

        assertEquals(DATA.plusDays(21), vacinacao.getDataFimCarencia());
        assertEquals(DATA.plusDays(180), vacinacao.getDataProximaDose());
        assertSame(animal, vacinacao.getAnimal());
        assertSame(vacina, vacinacao.getVacina());
        assertSame(aplicador, vacinacao.getUsuarioAplicador());
        assertSame(usuario, vacinacao.getUsuarioRegistro());
        assertEquals("L-01", vacinacao.getLoteVacina());
    }

    @Test
    void vacinaSemCarenciaLiberaOAnimalNoMesmoDia() {
        vacina.setCarenciaDias(0);

        Vacinacao vacinacao = vacinacaoService.create(dados(45.0), usuario);

        assertEquals(DATA, vacinacao.getDataFimCarencia());
    }

    @Test
    void vacinaSemIntervaloNaoTemProximaDose() {
        vacina.setIntervaloDoseDias(null);

        Vacinacao vacinacao = vacinacaoService.create(dados(45.0), usuario);

        assertNull(vacinacao.getDataProximaDose());
    }

    // ---------- Custo automático ----------

    @Test
    void deveGerarCustoAutomaticoDaVacina() {
        Custo custo = new Custo();
        when(custoAutomaticoService.sincronizarAutomatico(
                null, TipoCusto.VACINA, 45.0, DATA, animal))
                .thenReturn(custo);

        Vacinacao vacinacao = vacinacaoService.create(dados(45.0), usuario);

        assertSame(custo, vacinacao.getCusto());
    }

    @Test
    void vacinacaoSemValorNaoGeraCusto() {
        Vacinacao vacinacao = vacinacaoService.create(dados(null), usuario);

        assertNull(vacinacao.getCusto());
        verify(custoAutomaticoService).sincronizarAutomatico(
                null, TipoCusto.VACINA, null, DATA, animal);
    }

    @Test
    void atualizacaoRecalculaCarenciaEReaproveitaOCusto() {
        Custo custoExistente = new Custo();
        vacinacaoCadastrada(custoExistente);
        LocalDate novaData = DATA.plusDays(5);

        when(custoAutomaticoService.sincronizarAutomatico(
                custoExistente, TipoCusto.VACINA, 60.0, novaData, animal))
                .thenReturn(custoExistente);

        Vacinacao atualizada = vacinacaoService.update(
                VACINACAO_ID,
                new VacinacaoUpdateRequestDTO(
                        VACINA_ID, APLICADOR_ID, novaData, "L-02", 2.0, null, 60.0),
                usuario);

        assertEquals(novaData.plusDays(21), atualizada.getDataFimCarencia());
        assertSame(custoExistente, atualizada.getCusto());
    }

    @Test
    void exclusaoRemoveOCustoAutomatico() {
        Custo custo = new Custo();
        Vacinacao existente = vacinacaoCadastrada(custo);

        vacinacaoService.delete(VACINACAO_ID, usuario);

        verify(vacinacaoRepository).delete(existente);
        verify(custoAutomaticoService).excluirAutomatico(custo);
    }

    // ---------- Auxiliares ----------

    private VacinacaoRequestDTO dados(Double valor) {
        return new VacinacaoRequestDTO(
                BRINCO, VACINA_ID, APLICADOR_ID, DATA, " L-01 ", 2.0, null, valor);
    }

    private Vacinacao vacinacaoCadastrada(Custo custo) {
        Vacinacao vacinacao = new Vacinacao();
        vacinacao.setAnimal(animal);
        vacinacao.setCusto(custo);

        when(vacinacaoRepository.findById(VACINACAO_ID)).thenReturn(Optional.of(vacinacao));

        return vacinacao;
    }
}