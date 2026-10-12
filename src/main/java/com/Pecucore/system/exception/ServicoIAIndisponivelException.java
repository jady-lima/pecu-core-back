package com.Pecucore.system.exception;

public class ServicoIAIndisponivelException extends RuntimeException {
    public ServicoIAIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public ServicoIAIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
