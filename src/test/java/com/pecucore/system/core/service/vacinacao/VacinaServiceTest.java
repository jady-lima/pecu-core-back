package com.pecucore.system.core.service.vacinacao;

import com.pecucore.system.core.dto.vacinacao.VacinaRequestDTO;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.model.Vacina;
import com.pecucore.system.core.repository.VacinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VacinaService: vacinas e exames exigidos para o trânsito")
class VacinaServiceTest {

    private static final Long VACINA_ID = 1L;

    @Mock
    private VacinaRepository vacinaRepository;

    @Mock
    private VacinacaoConsultaService vacinacaoConsultaService;

    @InjectMocks
    private VacinaService vacinaService;

    @BeforeEach
    void setUp() {
        lenient().when(vacinaRepository.save(any(Vacina.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private static VacinaRequestDTO dados(Integer validadeDias, Boolean exigidaParaTransito) {
        return new VacinaRequestDTO(" exame aie ", "Laboratório de teste", 0, validadeDias, exigidaParaTransito);
    }

    @Nested
    @DisplayName("Requisito: marcar vacinas e exames exigidos em dia para o trânsito")
    class ExigidaParaTransito {

        @Test
        @DisplayName("Exame exigido com validade é cadastrado como exigido")
        void exigidaComValidade() {
            Vacina vacina = vacinaService.createVacina(dados(60, true));

            assertTrue(vacina.isExigidaParaTransito());
            assertEquals(60, vacina.getIntervaloDoseDias());
            assertEquals("EXAME AIE", vacina.getNome());
        }

        @Test
        @DisplayName("Sem informar o campo, a vacina não é exigida (compatível com o front atual)")
        void semInformarNaoExigida() {
            Vacina vacina = vacinaService.createVacina(dados(null, null));

            assertFalse(vacina.isExigidaParaTransito());
        }

        @Test
        @DisplayName("Exigida sem validade é recusada: não haveria como saber se venceu")
        void exigidaSemValidade() {
            assertThrows(RegraNegocioException.class, () -> vacinaService.createVacina(dados(null, true)));
            verify(vacinaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ao atualizar, a vacina pode deixar de ser exigida")
        void atualizarDesmarca() {
            Vacina existente = new Vacina();
            existente.setNome("EXAME AIE");
            existente.setIntervaloDoseDias(60);
            existente.setExigidaParaTransito(true);
            when(vacinaRepository.findById(VACINA_ID)).thenReturn(Optional.of(existente));

            Vacina atualizada = vacinaService.updateVacina(VACINA_ID, dados(60, false));

            assertFalse(atualizada.isExigidaParaTransito());
        }

        @Test
        @DisplayName("Lista só as vacinas e exames marcados como exigidos")
        void listaExigidas() {
            Vacina exigida = new Vacina();
            when(vacinaRepository.findByExigidaParaTransitoTrue()).thenReturn(List.of(exigida));

            assertEquals(List.of(exigida), vacinaService.getAllVacinasExigidasParaTransito());
        }
    }
}