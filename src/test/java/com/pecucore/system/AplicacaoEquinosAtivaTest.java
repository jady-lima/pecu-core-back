package com.pecucore.system;

import com.pecucore.system.bovinos.BovinosConfig;
import com.pecucore.system.bovinos.service.agrupamento.AgrupamentoBovinos;
import com.pecucore.system.bovinos.service.desempenho.GmdService;
import com.pecucore.system.core.dto.meta.MetaFaseRequestDTO;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.extensao.agrupamento.FaseLote;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.extensao.aplicacao.DescricaoAplicacao;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitaria;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.service.animal.AnimalService;
import com.pecucore.system.core.service.lote.LoteService;
import com.pecucore.system.core.service.lote.MetaFaseService;
import com.pecucore.system.equinos.service.agrupamento.AgrupamentoEquinos;
import com.pecucore.system.equinos.service.aplicacao.DescricaoAplicacaoEquinos;
import com.pecucore.system.equinos.service.desempenho.IndicadorPesoEquinos;
import com.pecucore.system.equinos.service.sanidade.RestricaoSanitariaEquinos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sobe o sistema inteiro com agrocore.aplicacao=equinos (banco H2 próprio) e verifica que o núcleo
 * passa a usar as regras dos equinos, sem nenhum componente dos bovinos.
 */
@SpringBootTest(properties = {
        "agrocore.aplicacao=equinos",
        "spring.datasource.url=jdbc:h2:mem:agrocore-equinos;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
@Transactional
@DisplayName("Framework: com agrocore.aplicacao=equinos, o sistema sobe com as regras dos equinos")
class AplicacaoEquinosAtivaTest {

    @Autowired
    private ApplicationContext contexto;

    @Autowired
    private IndicadorDesempenho indicadorDesempenho;

    @Autowired
    private PoliticaAgrupamento politicaAgrupamento;

    @Autowired
    private PoliticaRestricaoSanitaria politicaRestricaoSanitaria;

    @Autowired
    private DescricaoAplicacao descricaoAplicacao;

    @Autowired
    private LoteService loteService;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private MetaFaseService metaFaseService;

    private static Lote lote(String fase) {
        Lote lote = new Lote();
        lote.setFinalidade(fase);
        return lote;
    }

    @Test
    @DisplayName("Cada ponto flexível recebe a implementação dos equinos")
    void pontosFlexiveisDosEquinos() {
        assertInstanceOf(IndicadorPesoEquinos.class, indicadorDesempenho);
        assertInstanceOf(AgrupamentoEquinos.class, politicaAgrupamento);
        assertInstanceOf(RestricaoSanitariaEquinos.class, politicaRestricaoSanitaria);
        assertInstanceOf(DescricaoAplicacaoEquinos.class, descricaoAplicacao);
    }

    @Test
    @DisplayName("Nenhum componente dos bovinos é carregado")
    void bovinosDesligados() {
        assertTrue(contexto.getBeansOfType(BovinosConfig.class).isEmpty());
        assertTrue(contexto.getBeansOfType(GmdService.class).isEmpty());
        assertTrue(contexto.getBeansOfType(AgrupamentoBovinos.class).isEmpty());
    }

    @Test
    @DisplayName("O núcleo usa as fases, as saídas e a meta padrão (NRC 2007) dos equinos")
    void nucleoUsaRegrasDosEquinos() {
        assertEquals(List.of("POTRO", "JOVEM", "ADULTO", "REPRODUCAO", "OBSERVACAO"),
                loteService.getAllFasesLote().stream().map(FaseLote::codigo).toList());
        assertEquals(List.of(StatusAnimal.VENDIDO, StatusAnimal.OBITO), animalService.getAllSaidasAnimal());
        assertEquals(Optional.of(Meta.faixa(0.45, 0.85)), loteService.getMetaDesempenho(lote("POTRO")));
    }

    @Test
    @DisplayName("Meta cadastrada para a fase (no banco) substitui a meta padrão da aplicação")
    void metaCadastradaSubstituiPadrao() {
        metaFaseService.createMetaFase(new MetaFaseRequestDTO("POTRO", 0.5, 0.9));

        assertEquals(Optional.of(Meta.faixa(0.5, 0.9)), loteService.getMetaDesempenho(lote("POTRO")));
    }

    @Test
    @DisplayName("Meta para uma fase que só existe nos bovinos é recusada")
    void faseDosBovinosRecusada() {
        assertThrows(RegraNegocioException.class,
                () -> metaFaseService.createMetaFase(new MetaFaseRequestDTO("ENGORDA", 1.0, null)));
    }
}