package com.pecucore.system.core.service;

import com.pecucore.system.core.dto.alimentacao.AlimentacaoLoteRequestDTO;
import com.pecucore.system.core.dto.alimentacao.AlimentacaoRequestDTO;
import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.model.Alimentacao;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Custo;
import com.pecucore.system.core.model.TipoAlimentacao;
import com.pecucore.system.core.model.TipoCusto;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.AlimentacaoRepository;
import com.pecucore.system.core.service.custo.CustoAutomaticoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlimentacaoServiceTest {

    private static final int BRINCO = 123;
    private static final Long LOTE_ID = 5L;
    private static final Long ALIMENTACAO_ID = 60L;
    private static final LocalDate DATA = LocalDate.of(2026, 11, 1);

    @Mock
    private AlimentacaoRepository alimentacaoRepository;

    @Mock
    private AnimalService animalService;

    @Mock
    private LoteService loteService;

    @Mock
    private CustoAutomaticoService custoAutomaticoService;

    @InjectMocks
    private AlimentacaoService alimentacaoService;

    private Usuario usuario;
    private Animal animal;

    @BeforeEach
    void setUp() {
        usuario = mock(Usuario.class);

        animal = mock(Animal.class);
        lenient().when(animal.getId()).thenReturn(2L);

        lenient().when(animalService.getAnimalAtivoByBrinco(BRINCO, usuario)).thenReturn(animal);
        lenient().when(alimentacaoRepository.save(any(Alimentacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    void deveGerarCustoAutomaticoDaAlimentacao() {
        Custo custo = new Custo();
        when(custoAutomaticoService.sincronizarCustoAutomatico(
                null, TipoCusto.ALIMENTACAO, 80.0, DATA, animal))
                .thenReturn(custo);

        Alimentacao alimentacao = alimentacaoService.createAlimentacao(
                new AlimentacaoRequestDTO(BRINCO, TipoAlimentacao.RACAO, 12.5, DATA, 80.0),
                usuario);

        assertSame(custo, alimentacao.getCusto());
        assertEquals(TipoAlimentacao.RACAO, alimentacao.getTipo());
        assertEquals(DATA, alimentacao.getData());
        assertSame(animal, alimentacao.getAnimal());
    }

    @Test
    void alimentacaoSemValorNaoGeraCusto() {
        Alimentacao alimentacao = alimentacaoService.createAlimentacao(
                new AlimentacaoRequestDTO(BRINCO, TipoAlimentacao.PASTO, 12.5, DATA, null),
                usuario);

        assertNull(alimentacao.getCusto());
        verify(custoAutomaticoService).sincronizarCustoAutomatico(
                null, TipoCusto.ALIMENTACAO, null, DATA, animal);
    }

    @Test
    void alimentacaoPorLoteGeraUmRegistroEUmCustoPorAnimal() {
        Animal outroAnimal = mock(Animal.class);
        when(animalService.getAnimaisAtivosByLoteId(LOTE_ID))
                .thenReturn(List.of(animal, outroAnimal));

        List<Alimentacao> registros = alimentacaoService.createAlimentacaoEmLote(
                new AlimentacaoLoteRequestDTO(LOTE_ID, TipoAlimentacao.RACAO, 12.5, DATA, 80.0),
                usuario);

        assertEquals(2, registros.size());
        verify(loteService).getLoteById(LOTE_ID, usuario);
        verify(custoAutomaticoService).sincronizarCustoAutomatico(
                null, TipoCusto.ALIMENTACAO, 80.0, DATA, animal);
        verify(custoAutomaticoService).sincronizarCustoAutomatico(
                null, TipoCusto.ALIMENTACAO, 80.0, DATA, outroAnimal);
    }

    @Test
    void naoDeveRegistrarAlimentacaoEmLoteSemAnimaisAtivos() {
        when(animalService.getAnimaisAtivosByLoteId(LOTE_ID)).thenReturn(List.of());

        assertThrows(ConflitoNegocioException.class,
                () -> alimentacaoService.createAlimentacaoEmLote(
                        new AlimentacaoLoteRequestDTO(
                                LOTE_ID, TipoAlimentacao.RACAO, 12.5, DATA, 80.0),
                        usuario));

        verify(alimentacaoRepository, never()).save(any());
    }

    @Test
    void exclusaoRemoveOCustoAutomatico() {
        Custo custo = new Custo();
        Alimentacao existente = new Alimentacao();
        existente.setAnimal(animal);
        existente.setCusto(custo);
        when(alimentacaoRepository.findById(ALIMENTACAO_ID)).thenReturn(Optional.of(existente));

        alimentacaoService.deleteAlimentacao(ALIMENTACAO_ID, usuario);

        verify(alimentacaoRepository).delete(existente);
        verify(custoAutomaticoService).deleteCustoAutomatico(custo);
    }
}