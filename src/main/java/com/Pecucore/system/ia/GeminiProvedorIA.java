package com.Pecucore.system.ia;

import com.Pecucore.system.exception.ServicoIAIndisponivelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GeminiProvedorIA implements ProvedorIA {

    private static final String MIME_JSON = "application/json";
    private static final String FINALIZACAO_NORMAL = "STOP";

    @Autowired
    @Qualifier("geminiRestClient")
    private RestClient geminiRestClient;

    @Autowired
    private GeminiProperties geminiProperties;

    @Autowired
    private JsonMapper jsonMapper;

    @Override
    public RespostaIA gerarJson(PromptIA prompt) {
        if (!geminiProperties.possuiApiKey()) {
            throw new ServicoIAIndisponivelException("A integração com a IA não está configurada. Contate o administrador.");
        }

        GeminiResponse resposta = enviar(montarRequisicao(prompt));

        String texto = extrairTexto(resposta);
        String modelo = resposta.modelVersion() != null ? resposta.modelVersion() : geminiProperties.modelo();

        return new RespostaIA(texto, modelo);
    }

    private GeminiRequest montarRequisicao(PromptIA prompt) {
        return new GeminiRequest(
            new GeminiRequest.Conteudo(null, List.of(new GeminiRequest.Parte(prompt.instrucoes()))),
            List.of(new GeminiRequest.Conteudo("user", List.of(new GeminiRequest.Parte(prompt.conteudo())))),
            new GeminiRequest.ConfiguracaoGeracao(MIME_JSON, jsonMapper.readTree(prompt.esquemaJson()))
        );
    }

    private GeminiResponse enviar(GeminiRequest requisicao) {
        try {
            GeminiResponse resposta = geminiRestClient.post()
                    .uri("/models/{modelo}:generateContent", geminiProperties.modelo())
                    .header("x-goog-api-key", geminiProperties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requisicao)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (resposta == null) {
                throw new ServicoIAIndisponivelException("O serviço de IA retornou uma resposta vazia.");
            }

            return resposta;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new ServicoIAIndisponivelException("O limite de uso da IA foi atingido. Tente novamente em alguns minutos.", exception);
            }

            throw new ServicoIAIndisponivelException("O serviço de IA recusou a solicitação. Tente novamente mais tarde.", exception);
        } catch (ResourceAccessException exception) {
            throw new ServicoIAIndisponivelException("O serviço de IA não respondeu a tempo. Tente novamente mais tarde.", exception);
        } catch (RestClientException exception) {
            throw new ServicoIAIndisponivelException("Não foi possível se comunicar com o serviço de IA.", exception);
        }
    }

    private String extrairTexto(GeminiResponse resposta) {
        if (resposta.candidates() == null || resposta.candidates().isEmpty()) {
            throw new ServicoIAIndisponivelException("O serviço de IA não gerou nenhuma resposta.");
        }

        GeminiResponse.Candidato candidato = resposta.candidates().get(0);

        if (!FINALIZACAO_NORMAL.equals(candidato.finishReason())) {
            throw new ServicoIAIndisponivelException("O serviço de IA interrompeu a geração da análise (motivo: " + candidato.finishReason() + ").");
        }

        if (candidato.content() == null || candidato.content().parts() == null) {
            throw new ServicoIAIndisponivelException("O serviço de IA retornou uma resposta sem conteúdo.");
        }

        String texto = candidato.content().parts().stream()
                .map(GeminiResponse.Parte::text)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());

        if (texto.isBlank()) {
            throw new ServicoIAIndisponivelException("O serviço de IA retornou uma resposta sem conteúdo.");
        }

        return texto;
    }
}
