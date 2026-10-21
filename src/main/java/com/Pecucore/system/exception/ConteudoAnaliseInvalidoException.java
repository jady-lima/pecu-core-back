package com.Pecucore.system.exception;

public class ConteudoAnaliseInvalidoException extends RuntimeException {
    public ConteudoAnaliseInvalidoException(String mensagem) {
        super(mensagem);
    }

    public ConteudoAnaliseInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
