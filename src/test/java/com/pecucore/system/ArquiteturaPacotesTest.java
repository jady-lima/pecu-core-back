package com.pecucore.system;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ArquiteturaPacotesTest {

    private static final Path RAIZ = Path.of("src", "main", "java", "com", "pecucore", "system");
    private static final Path NUCLEO = RAIZ.resolve("core");
    private static final Path BOVINOS = RAIZ.resolve("bovinos");

    private static final Pattern IMPORT_BOVINOS = Pattern.compile(
            "^import\\s+com\\.pecucore\\.system\\.bovinos\\.([\\w.*]+);", Pattern.MULTILINE);

    /*
     * Dependências do núcleo para a aplicação bovinos que ainda existem.
     * Cada uma é eliminada numa refatoração da semana 2 (R1, R2 ou R3).
     * A lista só pode diminuir: ao resolver uma dependência, apague a linha dela.
     */
    private static final Set<String> DEPENDENCIAS_CONHECIDAS = Set.of(
            // R1 - Estratégia de desempenho
            "ContextoAnaliseService -> GmdService",
            "ContextoComparativoService -> GmdService",
            "DesempenhoService -> GmdService",
            "PesagemService -> GmdService",

            // R2 - Restrição sanitária
            "AnimalService -> CarenciaService",
            "ContextoAnaliseService -> CarenciaService",
            "MedicamentoService -> CalculadoraCarencia",
            "VacinacaoService -> CalculadoraCarencia",

            // R3 - Agrupamento
            "ContextoAnaliseService -> MetaDesempenhoService",
            "DesempenhoService -> MetaDesempenhoService",
            "Lote -> FinalidadeLote",
            "LoteRequestDTO -> FinalidadeLote",
            "LoteResponseDTO -> FinalidadeLote",
            "DadosAnimalDTO -> FinalidadeLote",
            "FichaAnimalResponseDTO -> FinalidadeLote",
            "ComparacaoDesempenhoResponseDTO -> FinalidadeLote"
    );

    @Test
    void todaClasseFicaNoNucleoOuNaAplicacao() throws IOException {
        List<String> foraDoLugar;

        try (Stream<Path> arquivos = Files.walk(RAIZ)) {
            foraDoLugar = arquivos
                    .filter(arquivo -> arquivo.toString().endsWith(".java"))
                    .filter(arquivo -> !arquivo.startsWith(NUCLEO) && !arquivo.startsWith(BOVINOS))
                    .filter(arquivo -> !arquivo.getFileName().toString().equals("SystemApplication.java"))
                    .map(arquivo -> RAIZ.relativize(arquivo).toString())
                    .sorted()
                    .toList();
        }

        assertTrue(foraDoLugar.isEmpty(),
                "Classes fora de core/ e bovinos/: " + foraDoLugar);
    }

    @Test
    void nucleoSoDependeDaAplicacaoBovinosNosPontosConhecidos() throws IOException {
        Set<String> encontradas = new TreeSet<>();

        try (Stream<Path> arquivos = Files.walk(NUCLEO)) {
            for (Path arquivo : arquivos.filter(a -> a.toString().endsWith(".java")).toList()) {
                String classeDoNucleo = arquivo.getFileName().toString().replace(".java", "");
                String conteudo = new String(Files.readAllBytes(arquivo), StandardCharsets.ISO_8859_1);

                Matcher matcher = IMPORT_BOVINOS.matcher(conteudo);
                while (matcher.find()) {
                    String importado = matcher.group(1);
                    String classeBovina = importado.endsWith(".*")
                            ? importado
                            : importado.substring(importado.lastIndexOf('.') + 1);

                    encontradas.add(classeDoNucleo + " -> " + classeBovina);
                }
            }
        }

        Set<String> novas = new TreeSet<>(encontradas);
        novas.removeAll(DEPENDENCIAS_CONHECIDAS);

        Set<String> resolvidas = new TreeSet<>(DEPENDENCIAS_CONHECIDAS);
        resolvidas.removeAll(encontradas);

        assertTrue(novas.isEmpty(),
                "O núcleo passou a depender da aplicação bovinos em: " + novas);
        assertTrue(resolvidas.isEmpty(),
                "Estas dependências foram resolvidas; apague-as de DEPENDENCIAS_CONHECIDAS: " + resolvidas);
    }
}