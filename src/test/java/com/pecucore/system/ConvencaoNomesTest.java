package com.pecucore.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Convenção de nomes do projeto (P1):
 * - CRUD: get/create/update/delete + nome do domínio em português
 *   (createAnimal, getAllLotes, getCustoById, updateStatusAlerta).
 * - Demais ações: verbo em português (calcularGmd, verificarQuedaPeso, montarIndividual).
 * Repositories ficam de fora: findBy/existsBy/countBy são exigidos pelo Spring Data.
 */
class ConvencaoNomesTest {

    private static final String[] PACOTES = {
            "com.pecucore.system.core",
            "com.pecucore.system.bovinos"
    };

    private static final Pattern CRUD_SEM_DOMINIO = Pattern.compile(
            "^(create|update|delete|getAll|get)(By[A-Z]\\w*)?$");

    private static final Pattern CRUD_EM_PORTUGUES = Pattern.compile(
            "^(criar|atualizar|listar|remover|excluir|alterar|registrar|buscar|consultar|salvar)([A-Z]\\w*)?$");

    private static final Pattern ACAO_EM_INGLES = Pattern.compile(
            "^(calculate|generate|validate|handle|find|save|remove|list|fetch|build|check|compute)([A-Z]\\w*)?$");

    private static final Set<String> EXCECOES = Set.of(
            "UsuarioService.loadUserByUsername" // exigido pela interface UserDetailsService
    );

    @Test
    void crudSempreIndicaODominio() throws ClassNotFoundException {
        verificar(CRUD_SEM_DOMINIO,
                "Métodos de CRUD sem o domínio (ex.: create -> createAnimal)");
    }

    @Test
    void crudUsaVerboEmIngles() throws ClassNotFoundException {
        verificar(CRUD_EM_PORTUGUES,
                "Métodos de CRUD com verbo em português (use get/create/update/delete)");
    }

    @Test
    void demaisAcoesUsamVerboEmPortugues() throws ClassNotFoundException {
        verificar(ACAO_EM_INGLES,
                "Ações com verbo em inglês fora do CRUD (ex.: calculateGmd -> calcularGmd)");
    }

    // ---------- Auxiliares ----------

    private void verificar(Pattern proibido, String mensagem) throws ClassNotFoundException {
        List<String> violacoes = new ArrayList<>();

        for (Class<?> classe : servicesEControllers()) {
            for (Method metodo : classe.getDeclaredMethods()) {
                if (!Modifier.isPublic(metodo.getModifiers()) || metodo.isSynthetic()) {
                    continue;
                }

                String nome = classe.getSimpleName() + "." + metodo.getName();

                if (!EXCECOES.contains(nome) && proibido.matcher(metodo.getName()).matches()) {
                    violacoes.add(nome);
                }
            }
        }

        assertTrue(violacoes.isEmpty(),
                mensagem + ": " + violacoes.stream().distinct().sorted().toList());
    }

    private static List<Class<?>> servicesEControllers() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<Class<?>> classes = new ArrayList<>();
        for (String pacote : PACOTES) {
            for (BeanDefinition definicao : scanner.findCandidateComponents(pacote)) {
                Class<?> classe = Class.forName(definicao.getBeanClassName());
                if (!classe.isMemberClass()) {
                    classes.add(classe);
                }
            }
        }
        return classes;
    }
}