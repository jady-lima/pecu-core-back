package com.Pecucore.system.ia;

import com.Pecucore.system.dto.analise.ContextoAnaliseAnimalDTO;
import com.Pecucore.system.dto.analise.RelatorioAnaliseDTO;
import com.Pecucore.system.ia.parser.RelatorioAnaliseParser;
import com.Pecucore.system.ia.prompt.PromptAnaliseBuilder;
import com.Pecucore.system.ia.provedor.ProvedorIA;
import com.Pecucore.system.ia.provedor.RespostaIA;
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
    public ResultadoAnaliseIA analisar(ContextoAnaliseAnimalDTO contexto) {
        RespostaIA resposta = provedorIA.gerarJson(promptAnaliseBuilder.montarIndividual(contexto));
        RelatorioAnaliseDTO relatorio = relatorioAnaliseParser.converterRespostaIA(resposta.texto());

        return new ResultadoAnaliseIA(relatorio, resposta.texto(), resposta.modelo());
    }

    @Override
    public RelatorioAnaliseDTO lerRelatorio(String conteudo) {
        return relatorioAnaliseParser.converterConteudoSalvo(conteudo);
    }
}
