package com.pecucore.system.core.config;

import com.pecucore.system.core.dto.erro.ErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Component
public class ErroSegurancaHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    public static final String MENSAGEM_NAO_AUTENTICADO =
            "Autenticação necessária. Faça login para continuar.";

    public static final String MENSAGEM_TOKEN_INVALIDO =
            "Token inválido ou expirado. Faça login novamente.";

    public static final String MENSAGEM_SEM_PERMISSAO =
            "Você não tem permissão para realizar esta ação.";

    @Autowired
    private JsonMapper jsonMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        responder(request, response, HttpStatus.UNAUTHORIZED, MENSAGEM_NAO_AUTENTICADO);
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        responder(request, response, HttpStatus.FORBIDDEN, MENSAGEM_SEM_PERMISSAO);
    }

    public void responderTokenInvalido(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        SecurityContextHolder.clearContext();
        responder(request, response, HttpStatus.UNAUTHORIZED, MENSAGEM_TOKEN_INVALIDO);
    }

    private void responder(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String mensagem
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(jsonMapper.writeValueAsString(
                ErroResponseDTO.de(status, mensagem, request.getRequestURI())));
    }
}