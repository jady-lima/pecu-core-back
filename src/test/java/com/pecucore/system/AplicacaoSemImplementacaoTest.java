package com.pecucore.system;

import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;
import com.pecucore.system.core.extensao.PontoFlexivelInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Uma aplicação prevista no enum, mas sem implementações (ovinos), não pode subir:
 * o ValidadorPontosFlexiveis do núcleo barra a inicialização e diz o que falta.
 */
@DisplayName("Framework: aplicação sem implementações dos pontos flexíveis não sobe")
class AplicacaoSemImplementacaoTest {

    @Test
    @DisplayName("Com agrocore.aplicacao=ovinos, o sistema se recusa a subir e lista os 4 pontos flexíveis faltando")
    void ovinosSemImplementacao() {
        // Argumentos de linha de comando têm prioridade sobre o application.properties de teste
        // (que liga os bovinos), como em: java -jar system.jar --agrocore.aplicacao=ovinos
        SpringApplicationBuilder aplicacao = new SpringApplicationBuilder(SystemApplication.class)
                .web(WebApplicationType.NONE);

        Throwable erro = assertThrows(Throwable.class, () -> {
            try (ConfigurableApplicationContext contexto = aplicacao.run(
                    "--agrocore.aplicacao=ovinos",
                    "--spring.datasource.url=jdbc:h2:mem:agrocore-ovinos;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")) {
                // Se o sistema subir (não deveria), o contexto é fechado aqui e o assertThrows falha.
            }
        });
        PontoFlexivelInvalidoException causa = encontrarCausa(erro);

        assertNotNull(causa, "Esperava PontoFlexivelInvalidoException, veio: " + erro);
        assertEquals(Aplicacao.OVINOS, causa.getAplicacao());
        assertEquals(List.of(
                "nenhuma implementação de DescricaoAplicacao",
                "nenhuma implementação de IndicadorDesempenho",
                "nenhuma implementação de PoliticaAgrupamento",
                "nenhuma implementação de PoliticaRestricaoSanitaria"
        ), causa.getProblemas());
    }

    private static PontoFlexivelInvalidoException encontrarCausa(Throwable erro) {
        for (Throwable atual = erro; atual != null; atual = atual.getCause()) {
            if (atual instanceof PontoFlexivelInvalidoException invalido) {
                return invalido;
            }
        }
        return null;
    }
}