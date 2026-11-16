package com.pecucore.system.core.extensao;

import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;

import java.util.List;

public class PontoFlexivelInvalidoException extends RuntimeException {

    private final Aplicacao aplicacao;
    private final List<String> problemas;

    public PontoFlexivelInvalidoException(Aplicacao aplicacao, List<String> problemas) {
        super("A aplicação " + aplicacao + " não atende aos pontos flexíveis do núcleo: " + problemas);
        this.aplicacao = aplicacao;
        this.problemas = List.copyOf(problemas);
    }

    public Aplicacao getAplicacao() {
        return aplicacao;
    }

    public List<String> getProblemas() {
        return problemas;
    }
}
