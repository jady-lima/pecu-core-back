package com.pecucore.system.core.extensao;

import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class ValidadorPontosFlexiveis implements BeanFactoryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        List<String> problemas = new ArrayList<>();

        for (Class<?> pontoFlexivel : buscarPontosFlexiveis(beanFactory.getBeanClassLoader())) {
            String[] implementacoes = beanFactory.getBeanNamesForType(pontoFlexivel, true, false);

            if (implementacoes.length == 0) {
                problemas.add("nenhuma implementação de " + pontoFlexivel.getSimpleName());
            } else if (implementacoes.length > 1) {
                problemas.add(implementacoes.length + " implementações de " + pontoFlexivel.getSimpleName()
                        + " " + Arrays.toString(implementacoes) + ", deve haver exatamente uma");
            }
        }

        if (!problemas.isEmpty()) {
            Aplicacao aplicacao = CondicaoAplicacaoAgroCore.lerAplicacaoAtiva(environment);
            throw new PontoFlexivelInvalidoException(aplicacao, problemas);
        }
    }

    private List<Class<?>> buscarPontosFlexiveis(ClassLoader classLoader) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definicao) {
                return definicao.getMetadata().isInterface();
            }
        };
        scanner.setEnvironment(environment);
        scanner.addIncludeFilter(new AnnotationTypeFilter(PontoFlexivel.class));

        return scanner.findCandidateComponents(PontoFlexivel.class.getPackageName()).stream()
                .map(BeanDefinition::getBeanClassName)
                .<Class<?>>map(nome -> ClassUtils.resolveClassName(nome, classLoader))
                .sorted(Comparator.comparing(Class::getSimpleName))
                .toList();
    }
}
