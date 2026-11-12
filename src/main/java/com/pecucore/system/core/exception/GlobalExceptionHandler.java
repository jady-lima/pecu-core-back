package com.pecucore.system.core.exception;

import com.pecucore.system.core.dto.erro.CampoInvalidoDTO;
import com.pecucore.system.core.dto.erro.ErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static final String MENSAGEM_DADOS_INVALIDOS = "Dados inválidos.";
    static final String MENSAGEM_SEM_PERMISSAO = "Você não tem permissão para realizar esta ação.";
    static final String MENSAGEM_ERRO_INTERNO = "Erro interno do servidor. Tente novamente mais tarde.";

    // ---------- Exceções do sistema ----------

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> handleRecursoNaoEncontrado(
            RecursoNaoEncontradoException exception, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(ConflitoNegocioException.class)
    public ResponseEntity<ErroResponseDTO> handleConflitoNegocio(
            ConflitoNegocioException exception, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> handleRegraNegocio(
            RegraNegocioException exception, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponseDTO> handleAcessoNegado(
            AcessoNegadoException exception, HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    @ExceptionHandler(DadosInsuficientesException.class)
    public ResponseEntity<ErroResponseDTO> handleDadosInsuficientes(
            DadosInsuficientesException exception, HttpServletRequest request) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), request);
    }

    @ExceptionHandler(ServicoIAIndisponivelException.class)
    public ResponseEntity<ErroResponseDTO> handleServicoIAIndisponivel(
            ServicoIAIndisponivelException exception, HttpServletRequest request) {
        log.error("Falha ao gerar análise com IA", exception);
        return responder(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request);
    }

    @ExceptionHandler(ConteudoAnaliseInvalidoException.class)
    public ResponseEntity<ErroResponseDTO> handleConteudoAnaliseInvalido(
            ConteudoAnaliseInvalidoException exception, HttpServletRequest request) {
        log.error("Conteúdo de análise salvo está inválido", exception);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível exibir a análise.", request);
    }

    // ---------- Segurança ----------

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDTO> handleAutenticacao(
            AuthenticationException exception, HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponseDTO> handleAccessDenied(
            AccessDeniedException exception, HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, MENSAGEM_SEM_PERMISSAO, request);
    }

    // ---------- Requisição inválida ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleValidacao(
            MethodArgumentNotValidException exception, HttpServletRequest request) {

        List<CampoInvalidoDTO> campos = exception.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoInvalidoDTO(erro.getField(), erro.getDefaultMessage()))
                .toList();

        String mensagem = campos.isEmpty() ? MENSAGEM_DADOS_INVALIDOS : campos.get(0).mensagem();

        return responder(HttpStatus.BAD_REQUEST, mensagem, request, campos);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponseDTO> handleValidacaoDeParametros(
            HandlerMethodValidationException exception, HttpServletRequest request) {

        String mensagem = exception.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(MENSAGEM_DADOS_INVALIDOS);

        return responder(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> handleCorpoInvalido(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido ou mal formatado.", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> handleTipoInvalido(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + exception.getName() + "'.", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> handleParametroAusente(
            MissingServletRequestParameterException exception, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "O parâmetro '" + exception.getParameterName() + "' é obrigatório.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> handleMetodoNaoSuportado(
            HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "O método " + exception.getMethod() + " não é suportado neste endereço.", request);
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErroResponseDTO> handleEnderecoInexistente(
            Exception exception, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "Endereço não encontrado.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> handleIntegridade(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("Violação de integridade de dados", exception);
        return responder(HttpStatus.CONFLICT,
                "A operação não pode ser concluída porque viola uma restrição dos dados.", request);
    }

    // ---------- Qualquer outro erro ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleErroInesperado(
            Exception exception, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), exception);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM_ERRO_INTERNO, request);
    }

    // ---------- Auxiliares ----------

    private ResponseEntity<ErroResponseDTO> responder(
            HttpStatus status, String mensagem, HttpServletRequest request) {
        return responder(status, mensagem, request, List.of());
    }

    private ResponseEntity<ErroResponseDTO> responder(
            HttpStatus status, String mensagem, HttpServletRequest request,
            List<CampoInvalidoDTO> campos) {
        return ResponseEntity
                .status(status)
                .body(ErroResponseDTO.de(status, mensagem, request.getRequestURI(), campos));
    }
}