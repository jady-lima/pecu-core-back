package com.pecucore.system.core.extensao;

import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

public class CondicaoAplicacaoAgroCore extends SpringBootCondition {

    static final String PROPRIEDADE = "agrocore.aplicacao";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> atributos = metadata.getAnnotationAttributes(AplicacaoAgroCore.class.getName());
        Aplicacao esperada = (Aplicacao) atributos.get("value");

        Aplicacao ativa = lerAplicacaoAtiva(context.getEnvironment());

        if (esperada == ativa) {
            return ConditionOutcome.match("Aplicação " + esperada + " ativa em " + PROPRIEDADE);
        }

        return ConditionOutcome.noMatch(
                "Aplicação " + esperada + " inativa: " + PROPRIEDADE + "=" + ativa
        );
    }

    static Aplicacao lerAplicacaoAtiva(Environment environment) {
        return Binder.get(environment)
                .bind(PROPRIEDADE, Aplicacao.class)
                .orElse(null);
    }
}
