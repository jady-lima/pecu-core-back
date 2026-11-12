package com.pecucore.system;

import com.jayway.jsonpath.JsonPath;
import com.pecucore.system.core.config.ErroSegurancaHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ErroSegurancaHandlerTest {

    private ErroSegurancaHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new ErroSegurancaHandler();
        ReflectionTestUtils.setField(handler, "jsonMapper", JsonMapper.builder().build());

        request = new MockHttpServletRequest("GET", "/animais");
        response = new MockHttpServletResponse();
    }

    @Test
    void semAutenticacaoDeveResponder401NoFormatoPadrao() throws Exception {
        handler.commence(request, response,
                new InsufficientAuthenticationException("sem token"));

        conferir(401, "UNAUTHORIZED", ErroSegurancaHandler.MENSAGEM_NAO_AUTENTICADO);
    }

    @Test
    void semPermissaoDeveResponder403NoFormatoPadrao() throws Exception {
        handler.handle(request, response, new AccessDeniedException("negado"));

        conferir(403, "FORBIDDEN", ErroSegurancaHandler.MENSAGEM_SEM_PERMISSAO);
    }

    @Test
    void tokenInvalidoDeveResponder401NoFormatoPadrao() throws Exception {
        handler.responderTokenInvalido(request, response);

        conferir(401, "UNAUTHORIZED", ErroSegurancaHandler.MENSAGEM_TOKEN_INVALIDO);
    }

    private void conferir(int statusEsperado, String erroEsperado, String mensagemEsperada)
            throws Exception {

        String json = response.getContentAsString(StandardCharsets.UTF_8);

        int status = JsonPath.read(json, "$.status");
        String erro = JsonPath.read(json, "$.erro");
        String mensagem = JsonPath.read(json, "$.mensagem");
        String caminho = JsonPath.read(json, "$.caminho");

        assertEquals(statusEsperado, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals(statusEsperado, status);
        assertEquals(erroEsperado, erro);
        assertEquals(mensagemEsperada, mensagem);
        assertEquals("/animais", caminho);
    }
}