package com.Pecucore.system.exception;

import com.Pecucore.system.dto.ErroResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDTO> handleAuthenticationException(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErroResponseDTO("Usuário ou senha inválidos"));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> handleRecursoNaoEncontradoException(
            RecursoNaoEncontradoException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponseDTO(exception.getMessage()));
    }


    @ExceptionHandler(ConflitoNegocioException.class)
    public ResponseEntity<ErroResponseDTO> handleConflitoNegocioException(
            ConflitoNegocioException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponseDTO(exception.getMessage()));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> handleRegraNegocioException(
            RegraNegocioException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO(exception.getMessage()));
    }


    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponseDTO> handleAcessoNegadoException(
            AcessoNegadoException exception) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErroResponseDTO(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleValidationException(MethodArgumentNotValidException exception) {
        String mensagem = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroResponseDTO(mensagem));
    }

    @ExceptionHandler(DadosInsuficientesException.class)
    public ResponseEntity<ErroResponseDTO> handleDadosInsuficientesException(DadosInsuficientesException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(new ErroResponseDTO(exception.getMessage()));
    }

    @ExceptionHandler(ServicoIAIndisponivelException.class)
    public ResponseEntity<ErroResponseDTO> handleServicoIAIndisponivelException(ServicoIAIndisponivelException exception) {
        log.error("Falha ao gerar análise com IA", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErroResponseDTO(exception.getMessage()));
    }

    @ExceptionHandler(ConteudoAnaliseInvalidoException.class)
    public ResponseEntity<ErroResponseDTO> handleConteudoAnaliseInvalidoException(ConteudoAnaliseInvalidoException exception) {
        log.error("Conteúdo de análise salvo está inválido", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErroResponseDTO("Não foi possível exibir a análise."));
    }
}
