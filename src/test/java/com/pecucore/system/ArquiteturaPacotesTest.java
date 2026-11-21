package com.pecucore.system;

import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;
import com.pecucore.system.core.extensao.AplicacaoAgroCore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regras de arquitetura do framework AgroCore:
 * - core/ é o núcleo; cada aplicação (bovinos/, equinos/, ...) fica no próprio pacote;
 * - o núcleo não conhece nenhuma aplicação, e as aplicações não se conhecem entre si;
 * - toda aplicação existente é ligada pela propriedade agrocore.aplicacao.
 * Aplicações previstas no enum Aplicacao, mas ainda sem pasta, são ignoradas.
 */
@DisplayName("Arquitetura: núcleo e aplicações do framework")
class ArquiteturaPacotesTest {

    private static final Path RAIZ = Path.of("src", "main", "java", "com", "pecucore", "system");

    @Test
    @DisplayName("Toda classe fica no núcleo ou no pacote de uma aplicação")
    void todaClasseFicaNoNucleoOuNumaAplicacao() throws IOException {
        List<Path> permitidos = Stream.concat(
                        Stream.of(ModulosAgroCore.NUCLEO),
                        ModulosAgroCore.pacotesDasAplicacoes().stream())
                .map(RAIZ::resolve)
                .toList();

        List<String> foraDoLugar;

        try (Stream<Path> arquivos = Files.walk(RAIZ)) {
            foraDoLugar = arquivos
                    .filter(arquivo -> arquivo.toString().endsWith(".java"))
                    .filter(arquivo -> permitidos.stream().noneMatch(arquivo::startsWith))
                    .filter(arquivo -> !arquivo.getFileName().toString().equals("SystemApplication.java"))
                    .map(arquivo -> RAIZ.relativize(arquivo).toString())
                    .sorted()
                    .toList();
        }

        assertTrue(foraDoLugar.isEmpty(),
                "Classes fora do núcleo e das aplicações " + permitidos + ": " + foraDoLugar);
    }

    @Test
    @DisplayName("O núcleo não depende de nenhuma aplicação")
    void nucleoNaoDependeDeNenhumaAplicacao() throws IOException {
        Map<String, Set<String>> dependencias =
                referenciasPara(ModulosAgroCore.NUCLEO, ModulosAgroCore.pacotesDasAplicacoes());

        assertTrue(dependencias.isEmpty(),
                "O núcleo referencia classes de aplicações: " + dependencias);
    }

    @Test
    @DisplayName("Uma aplicação não depende de outra aplicação")
    void aplicacoesNaoDependemUmasDasOutras() throws IOException {
        Map<String, Set<String>> dependencias = new TreeMap<>();

        for (String aplicacao : aplicacoesExistentes()) {
            List<String> outras = ModulosAgroCore.pacotesDasAplicacoes().stream()
                    .filter(outra -> !outra.equals(aplicacao))
                    .toList();

            dependencias.putAll(referenciasPara(aplicacao, outras));
        }

        assertTrue(dependencias.isEmpty(),
                "Aplicações referenciando outras aplicações: " + dependencias);
    }

    @Test
    @DisplayName("Toda aplicação existente é ligada por agrocore.aplicacao e importada no SystemApplication")
    void todaAplicacaoTemConfiguracaoAtivavel() {
        List<Class<?>> importadas = Arrays.asList(SystemApplication.class.getAnnotation(Import.class).value());
        List<String> problemas = new ArrayList<>();

        for (Aplicacao aplicacao : Aplicacao.values()) {
            String pacote = ModulosAgroCore.pacoteDa(aplicacao);

            if (!Files.isDirectory(RAIZ.resolve(pacote))) {
                continue; // aplicação prevista, ainda não implementada
            }

            List<Class<?>> configuracoes = configuracoesAtivadasPor(aplicacao);

            if (configuracoes.size() != 1) {
                problemas.add(pacote + ": esperada 1 classe com @AplicacaoAgroCore(" + aplicacao
                        + "), encontradas " + configuracoes);
            } else if (!importadas.contains(configuracoes.get(0))) {
                problemas.add(pacote + ": " + configuracoes.get(0).getSimpleName()
                        + " não está no @Import do SystemApplication");
            }
        }

        assertTrue(problemas.isEmpty(), String.join("; ", problemas));
    }

    // ---------- Auxiliares ----------

    private static List<String> aplicacoesExistentes() {
        return ModulosAgroCore.pacotesDasAplicacoes().stream()
                .filter(pacote -> Files.isDirectory(RAIZ.resolve(pacote)))
                .toList();
    }

    /** Para cada arquivo do pacote de origem, as referências (import ou nome completo) aos pacotes de destino. */
    private static Map<String, Set<String>> referenciasPara(String origem, List<String> destinos) throws IOException {
        Map<String, Set<String>> encontradas = new TreeMap<>();
        Path pastaOrigem = RAIZ.resolve(origem);

        if (destinos.isEmpty() || !Files.isDirectory(pastaOrigem)) {
            return encontradas;
        }

        Pattern referencia = Pattern.compile(
                ModulosAgroCore.BASE.replace(".", "\\.") + "\\.(" + String.join("|", destinos) + ")\\.[\\w.*]+");

        try (Stream<Path> arquivos = Files.walk(pastaOrigem)) {
            for (Path arquivo : arquivos.filter(a -> a.toString().endsWith(".java")).toList()) {
                String conteudo = Files.readString(arquivo, StandardCharsets.ISO_8859_1);
                Matcher matcher = referencia.matcher(conteudo);

                while (matcher.find()) {
                    encontradas
                            .computeIfAbsent(RAIZ.relativize(arquivo).toString(), chave -> new TreeSet<>())
                            .add(matcher.group());
                }
            }
        }

        return encontradas;
    }

    private static List<Class<?>> configuracoesAtivadasPor(Aplicacao aplicacao) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        // O scanner respeita @Conditional: sem agrocore.aplicacao, a CondicaoAplicacaoAgroCore descarta a classe.
        // Simulando a propriedade, o teste também prova que a configuração é ligada por ela.
        scanner.setEnvironment(new MockEnvironment().withProperty("agrocore.aplicacao", aplicacao.name()));
        scanner.addIncludeFilter(new AnnotationTypeFilter(AplicacaoAgroCore.class));

        String pacote = ModulosAgroCore.BASE + "." + ModulosAgroCore.pacoteDa(aplicacao);
        List<Class<?>> configuracoes = new ArrayList<>();

        for (BeanDefinition definicao : scanner.findCandidateComponents(pacote)) {
            Class<?> classe = ClassUtils.resolveClassName(definicao.getBeanClassName(), null);
            AplicacaoAgroCore anotacao = classe.getAnnotation(AplicacaoAgroCore.class);

            if (anotacao != null && anotacao.value() == aplicacao) {
                configuracoes.add(classe);
            }
        }

        return configuracoes;
    }
}