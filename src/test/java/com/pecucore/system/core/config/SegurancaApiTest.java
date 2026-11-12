package com.pecucore.system.core.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SegurancaApiTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(contexto)
                .apply(springSecurity())
                .build();
    }

    @Test
    void requisicaoSemTokenDeveResponder401() throws Exception {
        mockMvc.perform(get("/animais"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.mensagem").value(ErroSegurancaHandler.MENSAGEM_NAO_AUTENTICADO))
                .andExpect(jsonPath("$.caminho").value("/animais"));
    }

    @Test
    void tokenInvalidoDeveResponder401() throws Exception {
        mockMvc.perform(get("/animais").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.mensagem").value(ErroSegurancaHandler.MENSAGEM_TOKEN_INVALIDO));
    }

    @Test
    void perfilSemPermissaoDeveResponder403() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .with(user("veterinario")
                                .authorities(new SimpleGrantedAuthority("VETERINARIO"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("FORBIDDEN"))
                .andExpect(jsonPath("$.mensagem").value(ErroSegurancaHandler.MENSAGEM_SEM_PERMISSAO));
    }
}