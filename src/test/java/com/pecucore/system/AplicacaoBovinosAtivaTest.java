package com.pecucore.system;

import com.pecucore.system.bovinos.service.agrupamento.AgrupamentoBovinos;
import com.pecucore.system.bovinos.service.aplicacao.DescricaoAplicacaoBovinos;
import com.pecucore.system.bovinos.service.desempenho.GmdService;
import com.pecucore.system.bovinos.service.sanidade.RestricaoSanitariaBovinos;
import com.pecucore.system.core.dto.meta.MetaFaseRequestDTO;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.extensao.agrupamento.FaseLote;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.extensao.aplicacao.DescricaoAplicacao;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitaria;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.service.animal.AnimalService;
import com.pecucore.system.core.service.lote.LoteService;
import com.pecucore.system.core.service.lote.MetaFaseService;
import com.pecucore.system.equinos.EquinosConfig;
import com.pecucore.system.equinos.service.agrupamento.AgrupamentoEquinos;
import com.pecucore.system.equinos.service.desempenho.IndicadorPesoEquinos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sobe o sistema inteiro com agrocore.aplicacao=bovinos (banco H2 próprio) e verifica que o núcleo
 * usa as regras dos bovinos, sem nenhum componente dos equinos.
 */
@SpringBootTest(properties = {
        "agrocore.aplicacao=bovinos",
        "spring.datasource.url=jdbc:h2:mem:agrocore-bovinos;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
@Transactional
@DisplayName("Framework: com agrocore.aplicacao=bovinos, o sistema sobe com as regras dos bovinos")
class AplicacaoBovinosAtivaTest {

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

    @Test
    @DisplayName("Cada ponto flexível recebe a implementação dos bovinos")
    void pontosFlexiveisDosBovinos() {
        assertInstanceOf(GmdService.class, indicadorDesempenho);
        assertInstanceOf(AgrupamentoBovinos.class, politicaAgrupamento);
        assertInstanceOf(RestricaoSanitariaBovinos.class, politicaRestricaoSanitaria);
        assertInstanceOf(DescricaoAplicacaoBovinos.class, descricaoAplicacao);
    }

    @Test
    @DisplayName("Nenhum componente dos equinos é carregado")
    void equinosDesligados() {
        assertTrue(contexto.getBeansOfType(EquinosConfig.class).isEmpty());
        assertTrue(contexto.getBeansOfType(IndicadorPesoEquinos.class).isEmpty());
        assertTrue(contexto.getBeansOfType(AgrupamentoEquinos.class).isEmpty());
    }

    @Test
    @DisplayName("O núcleo usa as fases e as saídas dos bovinos (com abate)")
    void nucleoUsaRegrasDosBovinos() {
        assertEquals(List.of("CRIA", "RECRIA", "ENGORDA", "CORTE", "REPRODUCAO", "QUARENTENA", "DESMAME"),
                loteService.getAllFasesLote().stream().map(FaseLote::codigo).toList());
        assertEquals(List.of(StatusAnimal.VENDIDO, StatusAnimal.OBITO, StatusAnimal.ABATIDO),
                animalService.getAllSaidasAnimal());
    }

    @Test
    @DisplayName("Bovinos não têm meta padrão no código: sem meta cadastrada, o lote fica sem meta")
    void semMetaPadrao() {
        Lote lote = new Lote();
        lote.setFinalidade("ENGORDA");

        assertTrue(loteService.getMetaDesempenho(lote).isEmpty());
    }

    @Test
    @DisplayName("Meta para uma fase que só existe nos equinos é recusada")
    void faseDosEquinosRecusada() {
        assertThrows(RegraNegocioException.class,
                () -> metaFaseService.createMetaFase(new MetaFaseRequestDTO("POTRO", 0.5, 0.9)));
    }
}