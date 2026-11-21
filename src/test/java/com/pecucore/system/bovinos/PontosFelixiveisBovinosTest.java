package com.pecucore.system.bovinos;

import com.pecucore.system.bovinos.service.agrupamento.AgrupamentoBovinos;
import com.pecucore.system.bovinos.service.agrupamento.MetaDesempenhoService;
import com.pecucore.system.bovinos.service.aplicacao.DescricaoAplicacaoBovinos;
import com.pecucore.system.bovinos.service.desempenho.GmdService;
import com.pecucore.system.bovinos.service.sanidade.RestricaoSanitariaBovinos;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamentoContrato;
import com.pecucore.system.core.extensao.aplicacao.DescricaoAplicacao;
import com.pecucore.system.core.extensao.aplicacao.DescricaoAplicacaoContrato;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenhoContrato;
import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitaria;
import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitariaContrato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;

/**
 * A aplicação bovinos cumpre o contrato de cada ponto flexível do núcleo.
 * Os testes estão nos contratos (core/extensao/...); aqui só se informa qual é a implementação.
 */
@DisplayName("Bovinos cumprem os contratos dos pontos flexíveis do núcleo")
class PontosFlexiveisBovinosTest {

    @Nested
    @DisplayName("IndicadorDesempenho: GmdService")
    class Indicador extends IndicadorDesempenhoContrato {

        @Override
        protected IndicadorDesempenho criarIndicador() {
            return new GmdService();
        }
    }

    @Nested
    @DisplayName("PoliticaAgrupamento: AgrupamentoBovinos")
    class Agrupamento extends PoliticaAgrupamentoContrato {

        @Override
        protected PoliticaAgrupamento criarPolitica() {
            AgrupamentoBovinos agrupamento = new AgrupamentoBovinos();
            // A meta de cada fase vem do banco (MetaDesempenhoService); no contrato, o serviço é simulado.
            ReflectionTestUtils.setField(agrupamento, "metaDesempenhoService", mock(MetaDesempenhoService.class));
            return agrupamento;
        }
    }

    @Nested
    @DisplayName("PoliticaRestricaoSanitaria: RestricaoSanitariaBovinos")
    class RestricaoSanitaria extends PoliticaRestricaoSanitariaContrato {

        @Override
        protected PoliticaRestricaoSanitaria criarPolitica() {
            return new RestricaoSanitariaBovinos();
        }
    }

    @Nested
    @DisplayName("DescricaoAplicacao: DescricaoAplicacaoBovinos")
    class Descricao extends DescricaoAplicacaoContrato {

        @Override
        protected DescricaoAplicacao criarDescricao() {
            return new DescricaoAplicacaoBovinos();
        }
    }
}