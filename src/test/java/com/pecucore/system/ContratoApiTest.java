package com.pecucore.system;

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

import static org.junit.jupiter.api.Assertions.*;

class ContratoApiTest {

    private static final String[] PACOTES = {
            "com.pecucore.system.core",
            "com.pecucore.system.bovinos"
    };

    @Test
    void nenhumEndpointDevolveEntidadeJpa() throws ClassNotFoundException {
        List<String> violacoes = new ArrayList<>();

        for (Class<?> controller : controllers()) {
            for (Method metodo : endpoints(controller)) {
                if (contemEntidade(metodo.getGenericReturnType())) {
                    violacoes.add(controller.getSimpleName() + "." + metodo.getName());
                }
            }
        }

        assertTrue(violacoes.isEmpty(),
                "Endpoints que devolvem entidade JPA (use um DTO): " + violacoes);
    }

    // ---------- Auxiliares ----------

    static List<Class<?>> controllers() throws ClassNotFoundException {
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

    static List<Method> endpoints(Class<?> controller) {
        List<Method> metodos = new ArrayList<>();
        for (Method metodo : controller.getDeclaredMethods()) {
            if (AnnotatedElementUtils.hasAnnotation(metodo, RequestMapping.class)) {
                metodos.add(metodo);
            }
        }
        return metodos;
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