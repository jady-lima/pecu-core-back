package com.pecucore.system.core.ia;

import com.pecucore.system.core.dto.analise.ContextoAnaliseAnimalDTO;
import com.pecucore.system.core.dto.analise.ContextoAnaliseComparativaDTO;
import com.pecucore.system.core.dto.analise.RelatorioAnaliseDTO;
import com.pecucore.system.core.ia.parser.RelatorioAnaliseParser;
import com.pecucore.system.core.ia.prompt.PromptAnaliseBuilder;
import com.pecucore.system.core.ia.provedor.PromptIA;
import com.pecucore.system.core.ia.provedor.ProvedorIA;
import com.pecucore.system.core.ia.provedor.RespostaIA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnaliseAnimalIAFacadeImpl implements AnaliseAnimalIAFacade {

    @Autowired
    private PromptAnaliseBuilder promptAnaliseBuilder;

    @Autowired
    private ProvedorIA provedorIA;

    @Autowired
    private RelatorioAnaliseParser relatorioAnaliseParser;

    @Override
    public ResultadoAnaliseIA analisarIndividual(ContextoAnaliseAnimalDTO contexto) {
        return analisar(promptAnaliseBuilder.montarIndividual(contexto));
    }

    @Override
    public ResultadoAnaliseIA analisarComparativo(ContextoAnaliseComparativaDTO contexto) {
        return analisar(promptAnaliseBuilder.montarComparativo(contexto));
    }

    @Override
    public RelatorioAnaliseDTO lerRelatorio(String conteudo) {
        return relatorioAnaliseParser.converterConteudoSalvo(conteudo);
    }

    private ResultadoAnaliseIA analisar(PromptIA prompt) {
        RespostaIA resposta = provedorIA.gerarJson(prompt);
        RelatorioAnaliseDTO relatorio = relatorioAnaliseParser.converterRespostaIA(resposta.texto());

        return new ResultadoAnaliseIA(relatorio, resposta.texto(), resposta.modelo());
    }
}
