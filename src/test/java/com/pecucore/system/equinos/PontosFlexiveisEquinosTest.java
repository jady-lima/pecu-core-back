package com.pecucore.system.equinos;

import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamentoContrato;
import com.pecucore.system.core.extensao.aplicacao.DescricaoAplicacao;
import com.pecucore.system.core.extensao.aplicacao.DescricaoAplicacaoContrato;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenhoContrato;
import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitaria;
import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitariaContrato;
import com.pecucore.system.core.service.vacinacao.VacinaService;
import com.pecucore.system.core.service.vacinacao.VacinacaoConsultaService;
import com.pecucore.system.equinos.service.agrupamento.AgrupamentoEquinos;
import com.pecucore.system.equinos.service.aplicacao.DescricaoAplicacaoEquinos;
import com.pecucore.system.equinos.service.desempenho.IndicadorPesoEquinos;
import com.pecucore.system.equinos.service.sanidade.RestricaoSanitariaEquinos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.mockito.Mockito.mock;

/**
 * A aplicação equinos cumpre o contrato de cada ponto flexível do núcleo.
 * Os testes estão nos contratos (core/extensao/...); aqui só se informa qual é a implementação.
 */
@DisplayName("Equinos cumprem os contratos dos pontos flexíveis do núcleo")
class PontosFlexiveisEquinosTest {

    @Nested
    @DisplayName("IndicadorDesempenho: IndicadorPesoEquinos")
    class Indicador extends IndicadorDesempenhoContrato {

        @Override
        protected IndicadorDesempenho criarIndicador() {
            return new IndicadorPesoEquinos();
        }
    }

    @Nested
    @DisplayName("PoliticaAgrupamento: AgrupamentoEquinos")
    class Agrupamento extends PoliticaAgrupamentoContrato {

        @Override
        protected PoliticaAgrupamento criarPolitica() {
            return new AgrupamentoEquinos();
        }
    }

    @Nested
    @DisplayName("PoliticaRestricaoSanitaria: RestricaoSanitariaEquinos")
    class RestricaoSanitaria extends PoliticaRestricaoSanitariaContrato {

        @Override
        protected PoliticaRestricaoSanitaria criarPolitica() {
            // Sem vacinas/exames exigidos cadastrados: os mocks devolvem listas vazias.
            return new RestricaoSanitariaEquinos(mock(VacinaService.class), mock(VacinacaoConsultaService.class));
        }
    }

    @Nested
    @DisplayName("DescricaoAplicacao: DescricaoAplicacaoEquinos")
    class Descricao extends DescricaoAplicacaoContrato {

        @Override
        protected DescricaoAplicacao criarDescricao() {
            return new DescricaoAplicacaoEquinos();
        }
    }
}