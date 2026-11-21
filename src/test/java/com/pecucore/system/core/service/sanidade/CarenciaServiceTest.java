package com.pecucore.system.core.service.sanidade;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitaria;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.service.medicamento.MedicamentoConsultaService;
import com.pecucore.system.core.service.vacinacao.VacinacaoConsultaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Regressão do CarenciaService (núcleo).
 * Quais status ficam bloqueados é decisão da instância (ponto flexível PoliticaRestricaoSanitaria);
 * o núcleo só aplica o bloqueio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CarenciaService: período de carência sanitária")
class CarenciaServiceTest {

    private static final Long ANIMAL_ID = 7L;
    private static final LocalDate HOJE = LocalDate.now();
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Mock
    private VacinacaoConsultaService vacinacaoConsultaService;

    @Mock
    private MedicamentoConsultaService medicamentoConsultaService;

    @Mock
    private PoliticaRestricaoSanitaria politicaRestricaoSanitaria;

    @InjectMocks
    private CarenciaService carenciaService;

    private void dadaCarenciaDeVacina(LocalDate fim) {
        when(vacinacaoConsultaService.getDataFimCarenciaVigente(eq(ANIMAL_ID), any(LocalDate.class)))
                .thenReturn(Optional.ofNullable(fim));
    }

    private void dadaCarenciaDeMedicamento(LocalDate fim) {
        when(medicamentoConsultaService.getDataFimCarenciaVigente(eq(ANIMAL_ID), any(LocalDate.class)))
                .thenReturn(Optional.ofNullable(fim));
    }

    private void dadosStatusBloqueados(StatusAnimal... status) {
        when(politicaRestricaoSanitaria.getStatusBloqueadosNaCarencia()).thenReturn(Set.of(status));
    }

    @Nested
    @DisplayName("Requisito: calcular até quando o animal está em carência")
    class FimDaCarencia {

        @Test
        @DisplayName("Sem vacinação nem medicamento em carência: animal liberado")
        void semCarencia() {
            dadaCarenciaDeVacina(null);
            dadaCarenciaDeMedicamento(null);

            assertTrue(carenciaService.getDataFimCarencia(ANIMAL_ID).isEmpty());
            assertFalse(carenciaService.estaEmCarencia(ANIMAL_ID));
        }

        @Test
        @DisplayName("Só vacinação em carência: o fim é o da vacinação")
        void apenasVacina() {
            dadaCarenciaDeVacina(HOJE.plusDays(10));
            dadaCarenciaDeMedicamento(null);

            assertEquals(Optional.of(HOJE.plusDays(10)), carenciaService.getDataFimCarencia(ANIMAL_ID));
            assertTrue(carenciaService.estaEmCarencia(ANIMAL_ID));
        }

        @Test
        @DisplayName("Vacinação e medicamento em carência: vale a data mais distante")
        void vacinaEMedicamento() {
            dadaCarenciaDeVacina(HOJE.plusDays(10));
            dadaCarenciaDeMedicamento(HOJE.plusDays(25));

            assertEquals(Optional.of(HOJE.plusDays(25)), carenciaService.getDataFimCarencia(ANIMAL_ID));
        }
    }

    @Nested
    @DisplayName("Requisito: bloquear mudanças de status durante a carência (ponto flexível)")
    class BloqueioDeStatus {

        @Test
        @DisplayName("Status bloqueado pela política durante a carência: erro com a data de fim")
        void bloqueadoDuranteCarencia() {
            dadosStatusBloqueados(StatusAnimal.VENDIDO, StatusAnimal.ABATIDO);
            dadaCarenciaDeVacina(HOJE.plusDays(10));
            dadaCarenciaDeMedicamento(null);

            ConflitoNegocioException erro = assertThrows(ConflitoNegocioException.class,
                    () -> carenciaService.validarMudancaStatus(ANIMAL_ID, StatusAnimal.VENDIDO));

            assertTrue(erro.getMessage().contains(HOJE.plusDays(10).format(DATA)));
            assertTrue(erro.getMessage().contains("VENDIDO"));
        }

        @Test
        @DisplayName("Status bloqueado pela política, mas sem carência: mudança permitida")
        void bloqueadoSemCarencia() {
            dadosStatusBloqueados(StatusAnimal.VENDIDO, StatusAnimal.ABATIDO);
            dadaCarenciaDeVacina(null);
            dadaCarenciaDeMedicamento(null);

            assertDoesNotThrow(() -> carenciaService.validarMudancaStatus(ANIMAL_ID, StatusAnimal.VENDIDO));
        }

        @Test
        @DisplayName("Status não bloqueado pela política: nem consulta a carência")
        void statusNaoBloqueado() {
            dadosStatusBloqueados(StatusAnimal.VENDIDO, StatusAnimal.ABATIDO);

            assertDoesNotThrow(() -> carenciaService.validarMudancaStatus(ANIMAL_ID, StatusAnimal.OBITO));
            verifyNoInteractions(vacinacaoConsultaService, medicamentoConsultaService);
        }

        @Test
        @DisplayName("Quem decide os status bloqueados é a política da instância, não o núcleo")
        void politicaDecide() {
            dadosStatusBloqueados(StatusAnimal.OBITO);
            dadaCarenciaDeVacina(HOJE.plusDays(5));
            dadaCarenciaDeMedicamento(null);

            assertThrows(ConflitoNegocioException.class,
                    () -> carenciaService.validarMudancaStatus(ANIMAL_ID, StatusAnimal.OBITO));
            assertDoesNotThrow(() -> carenciaService.validarMudancaStatus(ANIMAL_ID, StatusAnimal.VENDIDO));
        }
    }
}