package com.pecucore.system;

import com.pecucore.system.core.config.AgroCoreConfig;
import com.pecucore.system.core.config.AgroCoreProperties;
import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AgroCorePropertiesTest {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(AgroCoreConfig.class);

    @Test
    void deveLerAAplicacaoAtiva() {
        contexto.withPropertyValues("agrocore.aplicacao=bovinos")
                .run(ctx -> assertThat(ctx.getBean(AgroCoreProperties.class).aplicacao())
                        .isEqualTo(Aplicacao.BOVINOS));
    }

    @Test
    void naoDeveSubirComAplicacaoDesconhecida() {
        contexto.withPropertyValues("agrocore.aplicacao=suinos")
                .run(ctx -> assertThat(ctx).hasFailed());
    }

    @Test
    void naoDeveSubirSemAplicacaoConfigurada() {
        contexto.run(ctx -> assertThat(ctx).hasFailed());
    }
}