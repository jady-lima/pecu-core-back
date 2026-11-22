package com.pecucore.system.core.service.lote;

import com.pecucore.system.core.dto.meta.MetaFaseRequestDTO;
import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.extensao.agrupamento.FaseLote;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.model.MetaFase;
import com.pecucore.system.core.repository.MetaFaseRepository;
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

/**
 * Metas por fase no núcleo. As fases (FASE_A, FASE_B) são fictícias: as reais vêm da aplicação ativa.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MetaFaseService: metas de desempenho cadastradas por fase")
class MetaFaseServiceTest {

    private static final Long META_ID = 1L;

    @Mock
    private MetaFaseRepository metaFaseRepository;

    @Mock
    private PoliticaAgrupamento politicaAgrupamento;

    @InjectMocks
    private MetaFaseService metaFaseService;

    @BeforeEach
    void setUp() {
        lenient().when(politicaAgrupamento.getFases())
                .thenReturn(List.of(new FaseLote("FASE_A", "Fase A"), new FaseLote("FASE_B", "Fase B")));
        lenient().when(metaFaseRepository.save(any(MetaFase.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private static MetaFase metaFase(String fase, Double minimo, Double maximo) {
        MetaFase metaFase = new MetaFase();
        metaFase.setFase(fase);
        metaFase.setMinimo(minimo);
        metaFase.setMaximo(maximo);
        return metaFase;
    }

    @Nested
    @DisplayName("Requisito: cadastrar a meta de uma fase da aplicação, como faixa")
    class Cadastro {

        @Test
        @DisplayName("Cadastra meta em faixa, inclusive com mínimo negativo")
        void cadastraFaixa() {
            MetaFase metaFase = metaFaseService.createMetaFase(new MetaFaseRequestDTO("FASE_A", -0.1, 0.1));

            assertEquals("FASE_A", metaFase.getFase());
            assertEquals(-0.1, metaFase.getMinimo());
            assertEquals(0.1, metaFase.getMaximo());
        }

        @Test
        @DisplayName("Fase que a aplicação não tem é recusada e as fases aceitas aparecem na mensagem")
        void faseDesconhecida() {
            RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                    () -> metaFaseService.createMetaFase(new MetaFaseRequestDTO("ENGORDA", 1.0, null)));

            assertTrue(erro.getMessage().contains("FASE_A, FASE_B"));
            verify(metaFaseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Mínimo maior que o máximo é recusado")
        void minimoMaiorQueMaximo() {
            assertThrows(RegraNegocioException.class,
                    () -> metaFaseService.createMetaFase(new MetaFaseRequestDTO("FASE_A", 0.5, 0.2)));
            verify(metaFaseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Só pode haver uma meta por fase")
        void umaMetaPorFase() {
            when(metaFaseRepository.findByFase("FASE_A")).thenReturn(Optional.of(metaFase("FASE_A", 1.0, null)));

            assertThrows(ConflitoNegocioException.class,
                    () -> metaFaseService.createMetaFase(new MetaFaseRequestDTO("FASE_A", 1.2, null)));
            verify(metaFaseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ao atualizar, não pode mudar para uma fase que já tem outra meta")
        void atualizarParaFaseOcupada() {
            when(metaFaseRepository.findById(META_ID)).thenReturn(Optional.of(metaFase("FASE_A", 1.0, null)));
            MetaFase outraMeta = mock(MetaFase.class);
            when(outraMeta.getId()).thenReturn(99L);
            when(metaFaseRepository.findByFase("FASE_B")).thenReturn(Optional.of(outraMeta));

            assertThrows(ConflitoNegocioException.class,
                    () -> metaFaseService.updateMetaFase(META_ID, new MetaFaseRequestDTO("FASE_B", 1.0, null)));
        }
    }

    @Nested
    @DisplayName("Requisito: informar a meta cadastrada para a fase")
    class Consulta {

        @Test
        @DisplayName("Fase com meta cadastrada: devolve a faixa")
        void faseComMeta() {
            when(metaFaseRepository.findByFase("FASE_A")).thenReturn(Optional.of(metaFase("FASE_A", 0.3, 0.6)));

            assertEquals(Optional.of(Meta.faixa(0.3, 0.6)), metaFaseService.getMetaByFase("FASE_A"));
        }

        @Test
        @DisplayName("Fase sem meta cadastrada: vazio")
        void faseSemMeta() {
            when(metaFaseRepository.findByFase("FASE_B")).thenReturn(Optional.empty());

            assertTrue(metaFaseService.getMetaByFase("FASE_B").isEmpty());
        }
    }
}