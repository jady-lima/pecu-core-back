package com.pecucore.system;

import com.pecucore.system.dto.analise.RelatorioAnaliseDTO;
import com.pecucore.system.exception.ConteudoAnaliseInvalidoException;
import com.pecucore.system.exception.ServicoIAIndisponivelException;
import com.pecucore.system.ia.parser.RelatorioAnaliseParser;
import com.pecucore.system.model.SituacaoGeralAnimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioAnaliseParserTest {

    private static final String RELATORIO_COMPLETO = """
            {
              "situacaoGeral": "ATENCAO",
              "resumo": "GMD abaixo da meta nas últimas pesagens.",
              "evolucaoDesempenho": "Queda de 1,2 para 0,8 kg/dia.",
              "pontosAtencao": ["GMD abaixo da meta"],
              "possiveisFatores": [],
              "recomendacoes": ["Sugere-se revisar a dieta."],
              "acompanhamento": [],
              "limitacoesDados": []
            }
            """;

    private RelatorioAnaliseParser parser;

    @BeforeEach
    void setUp() {
        parser = new RelatorioAnaliseParser();
        ReflectionTestUtils.setField(parser, "jsonMapper", JsonMapper.builder().build());
    }

    // ---------- Resposta vinda da IA ----------

    @Test
    void deveConverterRespostaCompletaDaIA() {
        RelatorioAnaliseDTO relatorio = parser.converterRespostaIA(RELATORIO_COMPLETO);

        assertEquals(SituacaoGeralAnimal.ATENCAO, relatorio.situacaoGeral());
        assertEquals("GMD abaixo da meta nas últimas pesagens.", relatorio.resumo());
        assertEquals(List.of("Sugere-se revisar a dieta."), relatorio.recomendacoes());
    }

    @Test
    void deveRejeitarRespostaSemSituacaoGeral() {
        assertThrows(ServicoIAIndisponivelException.class,
                () -> parser.converterRespostaIA("{\"resumo\":\"Animal bem\"}"));
    }

    @Test
    void deveRejeitarRespostaSemResumo() {
        assertThrows(ServicoIAIndisponivelException.class,
                () -> parser.converterRespostaIA("{\"situacaoGeral\":\"ESTAVEL\"}"));
    }

    @Test
    void deveRejeitarRespostaQueNaoEJson() {
        assertThrows(ServicoIAIndisponivelException.class,
                () -> parser.converterRespostaIA("O animal está bem."));
    }

    @Test
    void deveRejeitarSituacaoGeralDesconhecida() {
        assertThrows(ServicoIAIndisponivelException.class,
                () -> parser.converterRespostaIA(
                        "{\"situacaoGeral\":\"OTIMO\",\"resumo\":\"Animal bem\"}"));
    }

    // ---------- Conteúdo já salvo no histórico ----------

    @Test
    void deveRejeitarConteudoSalvoQueNaoEJson() {
        assertThrows(ConteudoAnaliseInvalidoException.class,
                () -> parser.converterConteudoSalvo("texto corrompido"));
    }

    @Test
    void deveRejeitarConteudoSalvoIncompleto() {
        assertThrows(ConteudoAnaliseInvalidoException.class,
                () -> parser.converterConteudoSalvo("{\"situacaoGeral\":\"ESTAVEL\"}"));
    }
}