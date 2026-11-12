package com.pecucore.system.core.config;

import com.pecucore.system.core.dto.erro.ErroResponseDTO;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AgroCore API",
                version = "1.0.0",
                description = "API do AgroCore, framework para gestão e acompanhamento de rebanhos. "
                        + "Aplicação ativa: PecuCore (bovinos de corte)."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenAPIConfig {

    private static final String SCHEMA_ERRO = "#/components/schemas/ErroResponseDTO";

    @Bean
    public OpenApiCustomizer errosNoFormatoPadrao() {
        return openApi -> {
            registrarSchemaDeErro(openApi);

            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().values().forEach(caminho ->
                    caminho.readOperations().forEach(this::padronizarErros));
        };
    }

    private void registrarSchemaDeErro(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }

        Components componentes = openApi.getComponents();

        ModelConverters.getInstance().readAll(ErroResponseDTO.class).forEach((nome, schema) -> {
            if (componentes.getSchemas() == null || !componentes.getSchemas().containsKey(nome)) {
                componentes.addSchemas(nome, schema);
            }
        });
    }

    private void padronizarErros(Operation operacao) {
        ApiResponses respostas = operacao.getResponses();

        if (respostas == null) {
            respostas = new ApiResponses();
            operacao.setResponses(respostas);
        }

        boolean exigeToken = operacao.getSecurity() != null && !operacao.getSecurity().isEmpty();

        if (exigeToken) {
            adicionarSeAusente(respostas, "401", "Não autenticado ou token inválido");
            adicionarSeAusente(respostas, "403", "Sem permissão para realizar esta ação");
        }

        adicionarSeAusente(respostas, "500", "Erro interno do servidor");

        respostas.forEach((codigo, resposta) -> {
            if (codigo.startsWith("4") || codigo.startsWith("5")) {
                resposta.setContent(conteudoDeErro());
            }
        });
    }

    private void adicionarSeAusente(ApiResponses respostas, String codigo, String descricao) {
        if (!respostas.containsKey(codigo)) {
            respostas.addApiResponse(codigo, new ApiResponse().description(descricao));
        }
    }

    private Content conteudoDeErro() {
        return new Content().addMediaType(
                "application/json",
                new MediaType().schema(new Schema<>().$ref(SCHEMA_ERRO)));
    }
}