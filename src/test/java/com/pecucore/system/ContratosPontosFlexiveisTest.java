package com.pecucore.system;

import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;
import com.pecucore.system.core.extensao.PontoFlexivel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Garante a cobertura dos contratos:
 * - todo ponto flexível X do núcleo tem a classe de teste XContrato, no mesmo pacote;
 * - toda aplicação existente tem PontosFlexiveis<Aplicacao>Test, com um @Nested para cada contrato.
 */
@DisplayName("Arquitetura: todo ponto flexível tem contrato, e toda aplicação o cumpre")
class ContratosPontosFlexiveisTest {

    private static final Path RAIZ = Path.of("src", "main", "java", "com", "pecucore", "system");

    @Test
    @DisplayName("Todo ponto flexível do núcleo tem uma classe de contrato <Interface>Contrato")
    void todoPontoFlexivelTemContrato() {
        List<String> semContrato = pontosFlexiveis().stream()
                .filter(ponto -> contratoDe(ponto).isEmpty())
                .map(Class::getSimpleName)
                .toList();

        assertTrue(semContrato.isEmpty(), "Pontos flexíveis sem contrato de teste: " + semContrato);
    }

    @Test
    @DisplayName("Toda aplicação existente cumpre o contrato de todos os pontos flexíveis")
    void todaAplicacaoCumpreTodosOsContratos() {
        List<String> problemas = new ArrayList<>();

        for (Aplicacao aplicacao : Aplicacao.values()) {
            String pacote = ModulosAgroCore.pacoteDa(aplicacao);

            if (!Files.isDirectory(RAIZ.resolve(pacote))) {
                continue; // aplicação prevista, ainda não implementada
            }

            String nomeTeste = ModulosAgroCore.BASE + "." + pacote + ".PontosFlexiveis" + capitalizar(pacote) + "Test";
            Optional<Class<?>> teste = carregar(nomeTeste);

            if (teste.isEmpty()) {
                problemas.add(pacote + ": falta a classe de teste " + nomeTeste);
                continue;
            }

            Set<Class<?>> contratosCumpridos = Arrays.stream(teste.get().getDeclaredClasses())
                    .map(Class::getSuperclass)
                    .collect(Collectors.toSet());

            for (Class<?> ponto : pontosFlexiveis()) {
                contratoDe(ponto)
                        .filter(contrato -> !contratosCumpridos.contains(contrato))
                        .ifPresent(contrato -> problemas.add(pacote + ": não testa " + contrato.getSimpleName()));
            }
        }

        assertTrue(problemas.isEmpty(), String.join("; ", problemas));
    }

    // ---------- Auxiliares ----------

    private static List<Class<?>> pontosFlexiveis() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definicao) {
                return definicao.getMetadata().isInterface();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(PontoFlexivel.class));

        return scanner.findCandidateComponents(PontoFlexivel.class.getPackageName()).stream()
                .<Class<?>>map(definicao -> ClassUtils.resolveClassName(definicao.getBeanClassName(), null))
                .sorted(Comparator.comparing(Class::getSimpleName))
                .toList();
    }

    private static Optional<Class<?>> contratoDe(Class<?> pontoFlexivel) {
        return carregar(pontoFlexivel.getName() + "Contrato");
    }

    private static Optional<Class<?>> carregar(String nomeClasse) {
        if (!ClassUtils.isPresent(nomeClasse, null)) {
            return Optional.empty();
        }
        return Optional.of(ClassUtils.resolveClassName(nomeClasse, null));
    }

    private static String capitalizar(String texto) {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}