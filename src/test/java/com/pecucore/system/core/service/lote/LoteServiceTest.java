package com.pecucore.system.core.service.lote;

import com.pecucore.system.core.dto.lote.LoteMetaDesempenhoRequestDTO;
import com.pecucore.system.core.dto.lote.LoteRequestDTO;
import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.extensao.agrupamento.FaseLote;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.Propriedade;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.LoteRepository;
import com.pecucore.system.core.service.animal.AnimalConsultaService;
import com.pecucore.system.core.service.propriedade.AcessoPropriedadeService;
import com.pecucore.system.core.service.propriedade.PropriedadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Regressão do LoteService (núcleo).
 * As fases (FASE_A, FASE_B) são fictícias de propósito: as fases reais vêm da instância
 * pelo ponto flexível PoliticaAgrupamento.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoteService: fases e meta de desempenho do lote")
class LoteServiceTest {

    private static final Long LOTE_ID = 10L;
    private static final Long PROPRIEDADE_ID = 20L;
    private static final LocalDate CRIACAO = LocalDate.of(2026, 1, 15);
    private static final List<FaseLote> FASES = List.of(
            new FaseLote("FASE_A", "Fase A"),
            new FaseLote("FASE_B", "Fase B")
    );

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private PropriedadeService propriedadeService;

    @Mock
    private AnimalConsultaService animalConsultaService;

    @Mock
    private AcessoPropriedadeService acessoPropriedadeService;

    @Mock
    private PoliticaAgrupamento politicaAgrupamento;

    @InjectMocks
    private LoteService loteService;

    private Usuario usuario;
    private Propriedade propriedade;

    @BeforeEach
    void setUp() {
        usuario = mock(Usuario.class);

        propriedade = mock(Propriedade.class);
        lenient().when(propriedade.getId()).thenReturn(PROPRIEDADE_ID);
        lenient().when(propriedadeService.getPropriedadeById(PROPRIEDADE_ID, usuario)).thenReturn(propriedade);

        lenient().when(politicaAgrupamento.getFases()).thenReturn(FASES);
        lenient().when(loteRepository.save(any(Lote.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private Lote lote(String finalidade, Double meta) {
        Lote lote = new Lote();
        lote.setNumero(1);
        lote.setFinalidade(finalidade);
        lote.setCapacidade(10);
        lote.setDataCriacao(CRIACAO);
        lote.setPropriedade(propriedade);
        lote.setMetaDesempenho(meta);
        return lote;
    }

    private Lote loteCadastrado(String finalidade, Double meta) {
        Lote lote = lote(finalidade, meta);
        when(loteRepository.findById(LOTE_ID)).thenReturn(Optional.of(lote));
        return lote;
    }

    private LoteRequestDTO dadosLote(String finalidade, int capacidade) {
        return new LoteRequestDTO(1, finalidade, capacidade, CRIACAO, PROPRIEDADE_ID);
    }

    @Nested
    @DisplayName("Requisito: meta de desempenho do lote (própria ou da fase)")
    class MetaDesempenho {

        @Test
        @DisplayName("Meta definida no lote tem prioridade sobre a meta da fase")
        void metaPropriaTemPrioridade() {
            assertEquals(Optional.of(1.1), loteService.getMetaDesempenho(lote("FASE_A", 1.1)));
            verify(politicaAgrupamento, never()).getMetaDaFase(anyString());
        }

        @Test
        @DisplayName("Sem meta própria, usa a meta da fase informada pela política da instância")
        void usaMetaDaFase() {
            when(politicaAgrupamento.getMetaDaFase("FASE_A")).thenReturn(Optional.of(0.9));

            assertEquals(Optional.of(0.9), loteService.getMetaDesempenho(lote("FASE_A", null)));
        }

        @Test
        @DisplayName("Sem meta própria e sem fase: não há meta")
        void semFinalidade() {
            assertTrue(loteService.getMetaDesempenho(lote(null, null)).isEmpty());
            verify(politicaAgrupamento, never()).getMetaDaFase(anyString());
        }

        @Test
        @DisplayName("Animal sem lote: não há meta")
        void semLote() {
            assertTrue(loteService.getMetaDesempenho(null).isEmpty());
        }

        @Test
        @DisplayName("Definir meta própria no lote")
        void definirMeta() {
            loteCadastrado("FASE_A", null);

            Lote lote = loteService.updateMetaDesempenho(
                    LOTE_ID, new LoteMetaDesempenhoRequestDTO(1.2), usuario);

            assertEquals(1.2, lote.getMetaDesempenho());
            assertEquals(Optional.of(1.2), loteService.getMetaDesempenho(lote));
        }

        @Test
        @DisplayName("Remover a meta própria faz o lote voltar a usar a meta da fase")
        void removerMeta() {
            Lote lote = loteCadastrado("FASE_A", 1.2);
            when(politicaAgrupamento.getMetaDaFase("FASE_A")).thenReturn(Optional.of(0.9));

            loteService.deleteMetaDesempenho(LOTE_ID, usuario);

            assertNull(lote.getMetaDesempenho());
            verify(loteRepository).save(lote);
            assertEquals(Optional.of(0.9), loteService.getMetaDesempenho(lote));
        }
    }

    @Nested
    @DisplayName("Requisito: as fases do lote vêm da instância (ponto flexível)")
    class Fases {

        @Test
        @DisplayName("Lista de fases é a da política de agrupamento")
        void listaVemDaPolitica() {
            assertEquals(FASES, loteService.getAllFasesLote());
        }

        @Test
        @DisplayName("Aceita lote com fase conhecida pela política")
        void aceitaFaseDaPolitica() {
            Lote lote = loteService.createLote(dadosLote("FASE_B", 10), usuario);

            assertEquals("FASE_B", lote.getFinalidade());
            assertSame(propriedade, lote.getPropriedade());
        }

        @Test
        @DisplayName("Recusa fase desconhecida e informa as fases aceitas")
        void rejeitaFaseDesconhecida() {
            RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                    () -> loteService.createLote(dadosLote("ENGORDA", 10), usuario));

            assertTrue(erro.getMessage().contains("FASE_A, FASE_B"));
            verify(loteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Requisito: integridade do lote (número, capacidade e exclusão)")
    class Integridade {

        @Test
        @DisplayName("Número de lote repetido na mesma propriedade: cadastro recusado")
        void numeroRepetido() {
            when(loteRepository.existsByNumeroAndPropriedade_Id(1, PROPRIEDADE_ID)).thenReturn(true);

            assertThrows(ConflitoNegocioException.class,
                    () -> loteService.createLote(dadosLote("FASE_A", 10), usuario));
            verify(loteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Capacidade não pode ficar menor que o número de animais ativos")
        void capacidadeMenorQueAtivos() {
            loteCadastrado("FASE_A", null);
            when(animalConsultaService.countAnimaisAtivosByLoteId(LOTE_ID)).thenReturn(5L);

            assertThrows(ConflitoNegocioException.class,
                    () -> loteService.updateLote(LOTE_ID, dadosLote("FASE_A", 3), usuario));
            verify(loteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lote com animais ativos não pode ser excluído")
        void naoExcluiComAtivos() {
            loteCadastrado("FASE_A", null);
            when(animalConsultaService.existsAnimalAtivoByLoteId(LOTE_ID)).thenReturn(true);

            assertThrows(ConflitoNegocioException.class,
                    () -> loteService.deleteLote(LOTE_ID, usuario));
            verify(loteRepository, never()).delete(any(Lote.class));
        }
    }
}