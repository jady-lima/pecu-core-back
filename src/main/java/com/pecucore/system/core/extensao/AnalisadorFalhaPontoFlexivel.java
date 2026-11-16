package com.pecucore.system.core.extensao;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import java.util.stream.Collectors;

public class AnalisadorFalhaPontoFlexivel extends AbstractFailureAnalyzer<PontoFlexivelInvalidoException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, PontoFlexivelInvalidoException causa) {
        String descricao = "A aplicação " + causa.getAplicacao()
                + " não atende aos pontos flexíveis do núcleo AgroCore:\n"
                + causa.getProblemas().stream()
                        .map(problema -> "  - " + problema)
                        .collect(Collectors.joining("\n"));

        String acao = "Implemente cada ponto flexível uma única vez no pacote da aplicação "
                + causa.getAplicacao()
                + " ou confira o valor de agrocore.aplicacao.";

        return new FailureAnalysis(descricao, acao, causa);
    }
}
