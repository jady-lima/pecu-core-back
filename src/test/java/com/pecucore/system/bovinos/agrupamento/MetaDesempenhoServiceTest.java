package com.pecucore.system.bovinos.agrupamento;

import com.pecucore.system.core.model.Lote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetaDesempenhoServiceTest {

    @Mock
    private MetaDesempenhoRepository metaDesempenhoRepository;

    @InjectMocks
    private MetaDesempenhoService metaDesempenhoService;

    @Test
    void deveUsarMetaDoLoteQuandoDefinida() {
        Lote lote = lote(1.8, FinalidadeLote.ENGORDA);

        assertEquals(Optional.of(1.8), metaDesempenhoService.getGmdEsperado(lote));

        verifyNoInteractions(metaDesempenhoRepository);
    }

    @Test
    void deveUsarMetaDaFaseQuandoLoteNaoTemMetaPropria() {
        Lote lote = lote(null, FinalidadeLote.ENGORDA);
        when(metaDesempenhoRepository.findByFase(FinalidadeLote.ENGORDA))
                .thenReturn(Optional.of(meta(FinalidadeLote.ENGORDA, 1.2)));

        assertEquals(Optional.of(1.2), metaDesempenhoService.getGmdEsperado(lote));
    }

    @Test
    void deveRetornarVazioQuandoNaoHaMetaNoLoteNemNaFase() {
        Lote lote = lote(null, FinalidadeLote.RECRIA);
        when(metaDesempenhoRepository.findByFase(FinalidadeLote.RECRIA))
                .thenReturn(Optional.empty());

        assertTrue(metaDesempenhoService.getGmdEsperado(lote).isEmpty());
    }

    @Test
    void deveRetornarVazioQuandoLoteNaoTemMetaNemFinalidade() {
        Lote lote = lote(null, null);

        assertTrue(metaDesempenhoService.getGmdEsperado(lote).isEmpty());

        verifyNoInteractions(metaDesempenhoRepository);
    }

    @Test
    void deveRetornarVazioQuandoAnimalNaoTemLote() {
        assertTrue(metaDesempenhoService.getGmdEsperado(null).isEmpty());

        verifyNoInteractions(metaDesempenhoRepository);
    }

    // ---------- Auxiliares ----------

    private Lote lote(Double gmdMeta, FinalidadeLote finalidade) {
        Lote lote = new Lote();
        lote.setGmdMeta(gmdMeta);
        lote.setFinalidade(finalidade);
        return lote;
    }

    private MetaDesempenho meta(FinalidadeLote fase, double gmdEsperado) {
        MetaDesempenho meta = new MetaDesempenho();
        meta.setFase(fase);
        meta.setGmdEsperado(gmdEsperado);
        return meta;
    }
}