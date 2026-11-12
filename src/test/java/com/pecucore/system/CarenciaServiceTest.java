package com.pecucore.system;

import com.pecucore.system.repository.MedicamentoRepository;
import com.pecucore.system.repository.VacinacaoRepository;
import com.pecucore.system.service.CarenciaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarenciaServiceTest {

    private static final Long ANIMAL_ID = 1L;

    @Mock
    private VacinacaoRepository vacinacaoRepository;

    @Mock
    private MedicamentoRepository medicamentoRepository;

    @InjectMocks
    private CarenciaService carenciaService;

    @Test
    void deveEstarEmCarenciaQuandoVacinaAindaEstaEmCarencia() {
        when(vacinacaoRepository.existsByAnimalIdAndDataFimCarenciaAfter(
                eq(ANIMAL_ID), any(LocalDate.class)))
                .thenReturn(true);

        assertTrue(carenciaService.estaEmCarencia(ANIMAL_ID));
    }

    @Test
    void deveEstarEmCarenciaQuandoMedicamentoAindaEstaEmCarencia() {
        when(vacinacaoRepository.existsByAnimalIdAndDataFimCarenciaAfter(
                eq(ANIMAL_ID), any(LocalDate.class)))
                .thenReturn(false);

        when(medicamentoRepository.existsByAnimalIdAndDataFimCarenciaAfter(
                eq(ANIMAL_ID), any(LocalDate.class)))
                .thenReturn(true);

        assertTrue(carenciaService.estaEmCarencia(ANIMAL_ID));
    }

    @Test
    void naoDeveEstarEmCarenciaSemVacinaNemMedicamentoEmCarencia() {
        when(vacinacaoRepository.existsByAnimalIdAndDataFimCarenciaAfter(
                eq(ANIMAL_ID), any(LocalDate.class)))
                .thenReturn(false);

        when(medicamentoRepository.existsByAnimalIdAndDataFimCarenciaAfter(
                eq(ANIMAL_ID), any(LocalDate.class)))
                .thenReturn(false);

        assertFalse(carenciaService.estaEmCarencia(ANIMAL_ID));
    }

    @Test
    void deveConsultarCarenciaComADataDeHoje() {
        carenciaService.estaEmCarencia(ANIMAL_ID);

        verify(vacinacaoRepository)
                .existsByAnimalIdAndDataFimCarenciaAfter(ANIMAL_ID, LocalDate.now());

        verify(medicamentoRepository)
                .existsByAnimalIdAndDataFimCarenciaAfter(ANIMAL_ID, LocalDate.now());
    }
}