package com.Pecucore.system.ia;

import java.util.List;

record GeminiResponse(
        List<Candidato> candidates,
        String modelVersion
) {

    record Candidato(Conteudo content, String finishReason) {
    }

    record Conteudo(List<Parte> parts) {
    }

    record Parte(String text) {
    }
}
