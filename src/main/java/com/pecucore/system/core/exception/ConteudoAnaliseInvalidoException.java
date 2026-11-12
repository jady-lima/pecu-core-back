package com.pecucore.system.core.exception;

public class ConteudoAnaliseInvalidoException extends RuntimeException {
    public ConteudoAnaliseInvalidoException(String mensagem) {
        super(mensagem);
    }

    public ConteudoAnaliseInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
