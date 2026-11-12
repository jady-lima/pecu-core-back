package com.pecucore.system;

import com.pecucore.system.bovinos.agrupamento.FinalidadeLote;
import com.pecucore.system.core.dto.desempenho.ComparacaoDesempenhoResponseDTO;
import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.*;
import com.pecucore.system.core.service.AnimalService;
import com.pecucore.system.core.service.DesempenhoService;
import com.pecucore.system.bovinos.agrupamento.MetaDesempenhoService;
import com.pecucore.system.bovinos.desempenho.GmdService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesempenhoServiceTest {

    private static final int BRINCO = 123;
    private static final LocalDate INICIO = LocalDate.of(2026, 9, 1);
    private static final double PRECISAO = 0.0001;

    @Mock
    private AnimalService animalService;

    @Mock
    private PesagemService pesagemService;

    @Mock
    private MetaDesempenhoService metaDesempenhoService;

    @Spy
    private GmdService gmdService = new GmdService();

    @InjectMocks
    private DesempenhoService desempenhoService;

    private Usuario usuario;
    private Lote lote;

    @BeforeEach
    void setUp() {
        usuario = mock(Usuario.class);

        lote = mock(Lote.class);
        lenient().when(lote.getFinalidade()).thenReturn(FinalidadeLote.ENGORDA);

        Animal animal = mock(Animal.class);
        lenient().when(animal.getBrinco()).thenReturn(BRINCO);
        lenient().when(animal.getLote()).thenReturn(lote);

        lenient().when(animalService.getAnimalByBrinco(BRINCO, usuario)).thenReturn(animal);
    }

    @Test
    void deveIndicarAnimalAbaixoDaMeta() {
        historico(
                pesagem(300, INICIO),
                pesagem(330, INICIO.plusDays(30)));
        metaDoLote(1.5);

        ComparacaoDesempenhoResponseDTO resultado = desempenhoService.comparar(BRINCO, usuario);

        assertEquals(BRINCO, resultado.brinco());
        assertEquals(FinalidadeLote.ENGORDA, resultado.fase());
        assertEquals(1.0, resultado.gmdAtual(), PRECISAO);
        assertEquals(1.5, resultado.gmdEsperado(), PRECISAO);
        assertEquals(-0.5, resultado.diferenca(), PRECISAO);
        assertEquals(66.6667, resultado.percentualDaMeta(), PRECISAO);
        assertEquals(33.3333, resultado.percentualAbaixoDaMeta(), PRECISAO);
        assertTrue(resultado.abaixoDaMeta());
    }

    @Test
    void deveIndicarAnimalAcimaDaMeta() {
        historico(
                pesagem(300, INICIO),
                pesagem(360, INICIO.plusDays(30)));
        metaDoLote(1.5);

        ComparacaoDesempenhoResponseDTO resultado = desempenhoService.comparar(BRINCO, usuario);

        assertEquals(2.0, resultado.gmdAtual(), PRECISAO);
        assertEquals(0.5, resultado.diferenca(), PRECISAO);
        assertEquals(133.3333, resultado.percentualDaMeta(), PRECISAO);
        assertEquals(-33.3333, resultado.percentualAbaixoDaMeta(), PRECISAO);
        assertFalse(resultado.abaixoDaMeta());
    }

    @Test
    void animalExatamenteNaMetaNaoEstaAbaixo() {
        historico(
                pesagem(300, INICIO),
                pesagem(345, INICIO.plusDays(30)));
        metaDoLote(1.5);

        ComparacaoDesempenhoResponseDTO resultado = desempenhoService.comparar(BRINCO, usuario);

        assertEquals(1.5, resultado.gmdAtual(), PRECISAO);
        assertFalse(resultado.abaixoDaMeta());
    }

    @Test
    void deveCalcularGmdEntreAPrimeiraEAUltimaPesagem() {
        historico(
                pesagem(300, INICIO),
                pesagem(290, INICIO.plusDays(10)),
                pesagem(330, INICIO.plusDays(30)));
        metaDoLote(1.5);

        ComparacaoDesempenhoResponseDTO resultado = desempenhoService.comparar(BRINCO, usuario);

        assertEquals(1.0, resultado.gmdAtual(), PRECISAO);
    }

    @Test
    void deveExigirPeloMenosDuasPesagens() {
        historico(pesagem(300, INICIO));

        assertThrows(ConflitoNegocioException.class,
                () -> desempenhoService.comparar(BRINCO, usuario));

        verifyNoInteractions(metaDesempenhoService);
    }

    @Test
    void deveRecusarPesagensNoMesmoDia() {
        historico(
                pesagem(300, INICIO),
                pesagem(310, INICIO));

        assertThrows(ConflitoNegocioException.class,
                () -> desempenhoService.comparar(BRINCO, usuario));

        verifyNoInteractions(metaDesempenhoService);
    }

    @Test
    void deveFalharQuandoNaoHaMetaParaOLote() {
        historico(
                pesagem(300, INICIO),
                pesagem(330, INICIO.plusDays(30)));
        when(metaDesempenhoService.getGmdEsperado(lote)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> desempenhoService.comparar(BRINCO, usuario));
    }

    // ---------- Auxiliares ----------

    private void historico(Pesagem... pesagens) {
        when(pesagemService.getHistorico(BRINCO, usuario)).thenReturn(List.of(pesagens));
    }

    private void metaDoLote(double gmdEsperado) {
        when(metaDesempenhoService.getGmdEsperado(lote)).thenReturn(Optional.of(gmdEsperado));
    }

    private Pesagem pesagem(double pesoKg, LocalDate data) {
        Pesagem pesagem = new Pesagem();
        pesagem.setPesoKg(pesoKg);
        pesagem.setData(data);
        return pesagem;
    }
}