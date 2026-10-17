package com.Pecucore.system.ia.provedor.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.JsonNode;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record GeminiRequest(
        Conteudo systemInstruction,
        List<Conteudo> contents,
        ConfiguracaoGeracao generationConfig
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Conteudo(String role, List<Parte> parts) {
    }

    record Parte(String text) {
    }

    record ConfiguracaoGeracao(String responseMimeType, JsonNode responseJsonSchema) {
    }
}
