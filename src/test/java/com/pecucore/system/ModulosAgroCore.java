package com.pecucore.system;

import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Pacotes do framework usados pelos testes de arquitetura.
 * O núcleo fica em core/; cada valor de AgroCoreProperties.Aplicacao tem (ou terá) o próprio pacote:
 * BOVINOS -> bovinos/, EQUINOS -> equinos/, OVINOS -> ovinos/.
 */
final class ModulosAgroCore {

    static final String BASE = "com.pecucore.system";
    static final String NUCLEO = "core";

    private ModulosAgroCore() {
    }

    static String pacoteDa(Aplicacao aplicacao) {
        return aplicacao.name().toLowerCase(Locale.ROOT);
    }

    static List<String> pacotesDasAplicacoes() {
        return Arrays.stream(Aplicacao.values())
                .map(ModulosAgroCore::pacoteDa)
                .toList();
    }

    /** Núcleo + todas as aplicações, no formato usado pelo scanner do Spring. */
    static String[] pacotesParaVarredura() {
        return Stream.concat(Stream.of(NUCLEO), pacotesDasAplicacoes().stream())
                .map(pacote -> BASE + "." + pacote)
                .toArray(String[]::new);
    }
}