package com.pecucore.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ContratoApiTest {

    private static final String[] PACOTES = {
            "com.pecucore.system.core",
            "com.pecucore.system.bovinos"
    };

    private static final Set<String> ENDPOINTS_PUBLICOS = Set.of(
            "AutenticacaoController.login"
    );

    @Test
    void nenhumEndpointDevolveEntidadeJpa() throws ClassNotFoundException {
        List<String> violacoes = new ArrayList<>();

        for (Class<?> controller : controllers()) {
            for (Method metodo : endpoints(controller)) {
                if (contemEntidade(metodo.getGenericReturnType())) {
                    violacoes.add(nome(controller, metodo));
                }
            }
        }

        assertTrue(violacoes.isEmpty(),
                "Endpoints que devolvem entidade JPA (use um DTO): " + violacoes);
    }

    @Test
    void todoControllerTemTagNoSwagger() throws ClassNotFoundException {
        List<String> semTag = new ArrayList<>();

        for (Class<?> controller : controllers()) {
            Tag tag = controller.getAnnotation(Tag.class);

            if (tag == null || tag.name().isBlank()) {
                semTag.add(controller.getSimpleName());
            }
        }

        assertTrue(semTag.isEmpty(), "Controllers sem @Tag: " + semTag);
    }

    @Test
    void todoEndpointTemResumoNoSwagger() throws ClassNotFoundException {
        List<String> semResumo = new ArrayList<>();

        for (Class<?> controller : controllers()) {
            for (Method metodo : endpoints(controller)) {
                Operation operacao = metodo.getAnnotation(Operation.class);

                if (operacao == null || operacao.summary().isBlank()) {
                    semResumo.add(nome(controller, metodo));
                }
            }
        }

        assertTrue(semResumo.isEmpty(),
                "Endpoints sem @Operation(summary = ...): " + semResumo);
    }

    @Test
    void todoEndpointProtegidoExigeTokenNoSwagger() throws ClassNotFoundException {
        List<String> semToken = new ArrayList<>();

        for (Class<?> controller : controllers()) {
            for (Method metodo : endpoints(controller)) {
                boolean exigeToken =
                        AnnotatedElementUtils.hasAnnotation(metodo, SecurityRequirement.class)
                                || AnnotatedElementUtils.hasAnnotation(controller, SecurityRequirement.class);

                if (!exigeToken && !ENDPOINTS_PUBLICOS.contains(nome(controller, metodo))) {
                    semToken.add(nome(controller, metodo));
                }
            }
        }

        assertTrue(semToken.isEmpty(),
                "Endpoints sem @SecurityRequirement(name = \"bearerAuth\"): " + semToken);
    }

    // ---------- Auxiliares ----------

    private static List<Class<?>> controllers() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<Class<?>> classes = new ArrayList<>();
        for (String pacote : PACOTES) {
            for (BeanDefinition definicao : scanner.findCandidateComponents(pacote)) {
                classes.add(Class.forName(definicao.getBeanClassName()));
            }
        }
        return classes;
    }

    private static List<Method> endpoints(Class<?> controller) {
        List<Method> metodos = new ArrayList<>();
        for (Method metodo : controller.getDeclaredMethods()) {
            if (AnnotatedElementUtils.hasAnnotation(metodo, RequestMapping.class)) {
                metodos.add(metodo);
            }
        }
        return metodos;
    }

    private static String nome(Class<?> controller, Method metodo) {
        return controller.getSimpleName() + "." + metodo.getName();
    }

    private boolean contemEntidade(Type tipo) {
        if (tipo instanceof Class<?> classe) {
            return classe.isAnnotationPresent(Entity.class);
        }

        if (tipo instanceof ParameterizedType parametrizado) {
            if (contemEntidade(parametrizado.getRawType())) {
                return true;
            }
            for (Type argumento : parametrizado.getActualTypeArguments()) {
                if (contemEntidade(argumento)) {
                    return true;
                }
            }
        }

        if (tipo instanceof WildcardType curinga) {
            for (Type limite : curinga.getUpperBounds()) {
                if (contemEntidade(limite)) {
                    return true;
                }
            }
        }

        if (tipo instanceof GenericArrayType arranjo) {
            return contemEntidade(arranjo.getGenericComponentType());
        }

        return false;
    }
}