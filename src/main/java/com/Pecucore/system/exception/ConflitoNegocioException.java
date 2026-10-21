package com.Pecucore.system.exception;

public class ConflitoNegocioException extends RuntimeException {

    public ConflitoNegocioException(String mensagem) {
        super(mensagem);
    }
}