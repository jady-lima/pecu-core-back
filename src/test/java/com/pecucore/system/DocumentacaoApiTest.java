package com.pecucore.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class DocumentacaoApiTest {

    private static final String SCHEMA_ERRO = "#/components/schemas/ErroResponseDTO";

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
    void errosDocumentadosUsamOFormatoPadrao() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.ErroResponseDTO.properties.mensagem").exists())
                .andExpect(jsonPath("$.components.schemas.ErroResponseDTO.properties.campos").exists())
                .andExpect(jsonPath(
                        "$.paths['/vacinas'].post.responses['400'].content['application/json'].schema['$ref']")
                        .value(SCHEMA_ERRO))
                .andExpect(jsonPath(
                        "$.paths['/animais/{id}'].get.responses['404'].content['application/json'].schema['$ref']")
                        .value(SCHEMA_ERRO));
    }

    @Test
    void endpointsProtegidosDocumentam401E403() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/alertas'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/alertas'].get.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/alertas'].get.responses['500']").exists())
                .andExpect(jsonPath("$.paths['/auth/login'].post.responses['403']").doesNotExist());
    }

    @Test
    void endpointsFicamAgrupadosPorTag() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/alertas'].get.tags[0]").value("Alertas"))
                .andExpect(jsonPath("$.paths['/animais'].get.tags[0]").value("Animais"))
                .andExpect(jsonPath("$.paths['/metas-desempenho'].get.tags[0]").value("Metas de desempenho"));
    }
}