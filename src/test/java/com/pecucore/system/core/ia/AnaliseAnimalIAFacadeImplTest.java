package com.pecucore.system.core.ia;

import com.pecucore.system.core.dto.analise.ContextoAnaliseAnimalDTO;
import com.pecucore.system.core.dto.analise.ContextoAnaliseComparativaDTO;
import com.pecucore.system.core.dto.analise.RelatorioAnaliseDTO;
import com.pecucore.system.core.exception.ServicoIAIndisponivelException;
import com.pecucore.system.core.ia.parser.RelatorioAnaliseParser;
import com.pecucore.system.core.ia.prompt.PromptAnaliseBuilder;
import com.pecucore.system.core.ia.provedor.PromptIA;
import com.pecucore.system.core.ia.provedor.ProvedorIA;
import com.pecucore.system.core.ia.provedor.RespostaIA;
import com.pecucore.system.core.model.SituacaoGeralAnimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnaliseAnimalIAFacadeImplTest {

    private static final String RESPOSTA_JSON = "{\"situacaoGeral\":\"ESTAVEL\"}";
    private static final String MODELO = "gemini-simulado";

    @Mock
    private PromptAnaliseBuilder promptAnaliseBuilder;

    @Mock
    private ProvedorIA provedorIA;

    @Mock
    private RelatorioAnaliseParser relatorioAnaliseParser;

    @InjectMocks
    private AnaliseAnimalIAFacadeImpl facade;

    private final PromptIA prompt = new PromptIA("instrucoes", "dados", "{}");

    @Test
    void deveInterpretarRespostaSimuladaDaIA() {
        ContextoAnaliseAnimalDTO contexto = contextoIndividual();
        RelatorioAnaliseDTO relatorio = relatorio();

        when(promptAnaliseBuilder.montarIndividual(contexto)).thenReturn(prompt);
        when(provedorIA.gerarJson(prompt)).thenReturn(new RespostaIA(RESPOSTA_JSON, MODELO));
        when(relatorioAnaliseParser.converterRespostaIA(RESPOSTA_JSON)).thenReturn(relatorio);

        ResultadoAnaliseIA resultado = facade.analisarIndividual(contexto);

        assertSame(relatorio, resultado.relatorio());
        assertEquals(RESPOSTA_JSON, resultado.conteudoBruto());
        assertEquals(MODELO, resultado.modelo());

        verify(provedorIA, times(1)).gerarJson(prompt);
    }

    @Test
    void devePropagarFalhaDoProvedor() {
        ContextoAnaliseAnimalDTO contexto = contextoIndividual();

        when(promptAnaliseBuilder.montarIndividual(contexto)).thenReturn(prompt);
        when(provedorIA.gerarJson(prompt))
                .thenThrow(new ServicoIAIndisponivelException("Serviço indisponível"));

        assertThrows(ServicoIAIndisponivelException.class,
                () -> facade.analisarIndividual(contexto));

        verifyNoInteractions(relatorioAnaliseParser);
    }

    @Test
    void deveAnalisarComparativoComProvedorSimulado() {
        ContextoAnaliseComparativaDTO contexto = new ContextoAnaliseComparativaDTO(
                LocalDate.now(), contextoIndividual(), null, null, List.of());
        RelatorioAnaliseDTO relatorio = relatorio();

        when(promptAnaliseBuilder.montarComparativo(contexto)).thenReturn(prompt);
        when(provedorIA.gerarJson(prompt)).thenReturn(new RespostaIA(RESPOSTA_JSON, MODELO));
        when(relatorioAnaliseParser.converterRespostaIA(RESPOSTA_JSON)).thenReturn(relatorio);

        ResultadoAnaliseIA resultado = facade.analisarComparativo(contexto);

        assertSame(relatorio, resultado.relatorio());
        assertEquals(MODELO, resultado.modelo());

        verify(provedorIA, times(1)).gerarJson(prompt);
        verify(promptAnaliseBuilder, never()).montarIndividual(any());
    }

    @Test
    void devePropagarRespostaInvalidaDaIA() {
        ContextoAnaliseAnimalDTO contexto = contextoIndividual();

        when(promptAnaliseBuilder.montarIndividual(contexto)).thenReturn(prompt);
        when(provedorIA.gerarJson(prompt)).thenReturn(new RespostaIA("resposta quebrada", MODELO));
        when(relatorioAnaliseParser.converterRespostaIA("resposta quebrada"))
                .thenThrow(new ServicoIAIndisponivelException("Formato inválido"));

        assertThrows(ServicoIAIndisponivelException.class,
                () -> facade.analisarIndividual(contexto));
    }

    @Test
    void deveLerRelatorioSalvoSemChamarAIA() {
        RelatorioAnaliseDTO relatorio = relatorio();
        when(relatorioAnaliseParser.converterConteudoSalvo("{salvo}")).thenReturn(relatorio);

        assertSame(relatorio, facade.lerRelatorio("{salvo}"));

        verifyNoInteractions(provedorIA, promptAnaliseBuilder);
    }

    // ---------- Auxiliares ----------

    private ContextoAnaliseAnimalDTO contextoIndividual() {
        return new ContextoAnaliseAnimalDTO(
                LocalDate.now(), null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(),
                null);
    }

    private RelatorioAnaliseDTO relatorio() {
        return new RelatorioAnaliseDTO(
                SituacaoGeralAnimal.ESTAVEL,
                "Animal estável",
                "Desempenho adequado",
                List.of(), List.of(), List.of(), List.of(), List.of());
    }
}