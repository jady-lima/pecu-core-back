package com.pecucore.system.core.service;

import com.pecucore.system.bovinos.sanidade.CalculadoraCarencia;
import com.pecucore.system.core.dto.medicamento.MedicamentoRequestDTO;
import com.pecucore.system.core.dto.medicamento.MedicamentoUpdateRequestDTO;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Custo;
import com.pecucore.system.core.model.Medicamento;
import com.pecucore.system.core.model.TipoCusto;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.MedicamentoRepository;
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
class MedicamentoServiceTest {

    private static final int BRINCO = 123;
    private static final Long MEDICAMENTO_ID = 40L;
    private static final LocalDate DATA = LocalDate.of(2026, 11, 1);

    @Mock
    private MedicamentoRepository medicamentoRepository;

    @Mock
    private AnimalService animalService;

    @Mock
    private CustoAutomaticoService custoAutomaticoService;

    @Spy
    private CalculadoraCarencia calculadoraCarencia = new CalculadoraCarencia();

    @InjectMocks
    private MedicamentoService medicamentoService;

    private Usuario usuario;
    private Animal animal;

    @BeforeEach
    void setUp() {
        usuario = mock(Usuario.class);

        animal = mock(Animal.class);
        lenient().when(animal.getId()).thenReturn(2L);

        lenient().when(animalService.getAnimalAtivoByBrinco(BRINCO, usuario)).thenReturn(animal);
        lenient().when(medicamentoRepository.save(any(Medicamento.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    // ---------- Carência ----------

    @Test
    void deveCalcularFimDaCarenciaDoMedicamento() {
        Medicamento medicamento = medicamentoService.createMedicamento(dados(10, 30.0), usuario);

        assertEquals(DATA.plusDays(10), medicamento.getDataFimCarencia());
        assertEquals(10, medicamento.getCarenciaDias());
        assertEquals("Ivermectina", medicamento.getNome());
        assertSame(animal, medicamento.getAnimal());
    }

    @Test
    void medicamentoSemCarenciaLiberaOAnimalNoMesmoDia() {
        Medicamento medicamento = medicamentoService.createMedicamento(dados(0, 30.0), usuario);

        assertEquals(DATA, medicamento.getDataFimCarencia());
    }

    // ---------- Custo automático ----------

    @Test
    void deveGerarCustoAutomaticoDoMedicamento() {
        Custo custo = new Custo();
        when(custoAutomaticoService.sincronizarCustoAutomatico(
                null, TipoCusto.MEDICAMENTO, 30.0, DATA, animal))
                .thenReturn(custo);

        Medicamento medicamento = medicamentoService.createMedicamento(dados(10, 30.0), usuario);

        assertSame(custo, medicamento.getCusto());
    }

    @Test
    void medicamentoSemValorNaoGeraCusto() {
        Medicamento medicamento = medicamentoService.createMedicamento(dados(10, null), usuario);

        assertNull(medicamento.getCusto());
        verify(custoAutomaticoService).sincronizarCustoAutomatico(
                null, TipoCusto.MEDICAMENTO, null, DATA, animal);
    }

    @Test
    void atualizacaoRecalculaCarenciaEReaproveitaOCusto() {
        Custo custoExistente = new Custo();
        medicamentoCadastrado(custoExistente);
        LocalDate novaData = DATA.plusDays(5);

        when(custoAutomaticoService.sincronizarCustoAutomatico(
                custoExistente, TipoCusto.MEDICAMENTO, 50.0, novaData, animal))
                .thenReturn(custoExistente);

        Medicamento atualizado = medicamentoService.updateMedicamento(
                MEDICAMENTO_ID,
                new MedicamentoUpdateRequestDTO(
                        "Ivermectina", "Vermifugacao", 5.0, 14, novaData, 50.0),
                usuario);

        assertEquals(novaData.plusDays(14), atualizado.getDataFimCarencia());
        assertSame(custoExistente, atualizado.getCusto());
    }

    @Test
    void exclusaoRemoveOCustoAutomatico() {
        Custo custo = new Custo();
        Medicamento existente = medicamentoCadastrado(custo);

        medicamentoService.deleteMedicamento(MEDICAMENTO_ID, usuario);

        verify(medicamentoRepository).delete(existente);
        verify(custoAutomaticoService).deleteCustoAutomatico(custo);
    }

    // ---------- Auxiliares ----------

    private MedicamentoRequestDTO dados(int carenciaDias, Double valor) {
        return new MedicamentoRequestDTO(
                BRINCO, " Ivermectina ", " Vermifugacao ", 5.0, carenciaDias, DATA, valor);
    }

    private Medicamento medicamentoCadastrado(Custo custo) {
        Medicamento medicamento = new Medicamento();
        medicamento.setAnimal(animal);
        medicamento.setCusto(custo);

        when(medicamentoRepository.findById(MEDICAMENTO_ID)).thenReturn(Optional.of(medicamento));

        return medicamento;
    }
}