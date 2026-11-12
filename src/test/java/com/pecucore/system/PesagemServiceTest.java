package com.pecucore.system;

import com.pecucore.system.dto.pesagem.PesagemRequestDTO;
import com.pecucore.system.exception.ConflitoNegocioException;
import com.pecucore.system.model.Animal;
import com.pecucore.system.model.Pesagem;
import com.pecucore.system.model.Usuario;
import com.pecucore.system.repository.PesagemRepository;
import com.pecucore.system.service.AnimalService;
import com.pecucore.system.service.alerta.AlertaPesoService;
import com.pecucore.system.service.calculo.GmdService;
import com.pecucore.system.service.pesagem.PesagemService;
import com.pecucore.system.service.pesagem.ResultadoPesagem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PesagemServiceTest {

    private static final Long ANIMAL_ID = 2L;
    private static final int BRINCO = 123;
    private static final int INTERVALO_RECOMENDADO_DIAS = 90;
    private static final LocalDate DATA_ANTERIOR = LocalDate.of(2026, 9, 1);
    private static final double PRECISAO = 0.0001;

    @Mock
    private PesagemRepository pesagemRepository;

    @Mock
    private AnimalService animalService;

    @Mock
    private AlertaPesoService alertaPesoService;

    @Spy
    private GmdService gmdService = new GmdService();

    @InjectMocks
    private PesagemService pesagemService;

    private Usuario usuario;
    private Animal animal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                pesagemService, "intervaloRecomendadoDias", INTERVALO_RECOMENDADO_DIAS);

        usuario = mock(Usuario.class);

        animal = mock(Animal.class);
        lenient().when(animal.getId()).thenReturn(ANIMAL_ID);

        lenient().when(animalService.getAnimalAtivoByBrinco(BRINCO, usuario)).thenReturn(animal);
    }

    @Test
    void deveCalcularGmdComBaseNaUltimaPesagemEDispararAlertas() {
        ultimaPesagem(300, DATA_ANTERIOR);

        ResultadoPesagem resultado = registrar(314.0, DATA_ANTERIOR.plusDays(7));

        Pesagem nova = resultado.pesagem();
        assertEquals(314.0, nova.getPesoKg(), PRECISAO);
        assertEquals(2.0, nova.getGmdCalculado(), PRECISAO);
        assertEquals(DATA_ANTERIOR.plusDays(7), nova.getData());
        assertSame(animal, nova.getAnimal());
        assertNull(resultado.aviso());

        verify(pesagemRepository).save(nova);
        verify(animalService).atualizarPesoAtual(animal, 314.0);
        verify(alertaPesoService).verificarGmdAbaixoMeta(eq(animal), doubleThat(gmd -> Math.abs(gmd - 2.0) < PRECISAO));
        verify(alertaPesoService).verificarQuedaPeso(animal);
    }

    @Test
    void primeiraPesagemNaoTemGmd() {
        when(pesagemRepository.findTopByAnimalIdOrderByDataDescIdDesc(ANIMAL_ID))
                .thenReturn(Optional.empty());

        ResultadoPesagem resultado = registrar(300.0, DATA_ANTERIOR);

        assertNull(resultado.pesagem().getGmdCalculado());
        verify(alertaPesoService).verificarGmdAbaixoMeta(animal, null);
    }

    @Test
    void pesagemNoMesmoDiaDaAnteriorNaoTemGmd() {
        ultimaPesagem(300, DATA_ANTERIOR);

        ResultadoPesagem resultado = registrar(305.0, DATA_ANTERIOR);

        assertNull(resultado.pesagem().getGmdCalculado());
    }

    @Test
    void naoDeveAceitarPesagemComDataAnteriorAUltima() {
        ultimaPesagem(300, DATA_ANTERIOR);

        assertThrows(ConflitoNegocioException.class,
                () -> registrar(310.0, DATA_ANTERIOR.minusDays(1)));

        verify(pesagemRepository, never()).save(any());
        verifyNoInteractions(alertaPesoService);
    }

    @Test
    void deveAvisarQuandoIntervaloPassaDoRecomendado() {
        ultimaPesagem(300, DATA_ANTERIOR);

        ResultadoPesagem resultado = registrar(380.0, DATA_ANTERIOR.plusDays(91));

        assertNotNull(resultado.aviso());
        assertTrue(resultado.aviso().contains("91 dias"));
    }

    @Test
    void naoDeveAvisarNoIntervaloExatoRecomendado() {
        ultimaPesagem(300, DATA_ANTERIOR);

        ResultadoPesagem resultado = registrar(380.0, DATA_ANTERIOR.plusDays(90));

        assertNull(resultado.aviso());
    }

    // ---------- Auxiliares ----------

    private ResultadoPesagem registrar(double pesoKg, LocalDate data) {
        return pesagemService.create(new PesagemRequestDTO(BRINCO, pesoKg, data), usuario);
    }

    private void ultimaPesagem(double pesoKg, LocalDate data) {
        Pesagem pesagem = new Pesagem();
        pesagem.setPesoKg(pesoKg);
        pesagem.setData(data);

        when(pesagemRepository.findTopByAnimalIdOrderByDataDescIdDesc(ANIMAL_ID))
                .thenReturn(Optional.of(pesagem));
    }
}