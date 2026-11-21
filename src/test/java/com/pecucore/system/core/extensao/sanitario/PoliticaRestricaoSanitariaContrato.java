package com.pecucore.system.core.extensao.sanitario;

import com.pecucore.system.core.model.StatusAnimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contrato do ponto flexível PoliticaRestricaoSanitaria: o que o núcleo espera de qualquer implementação.
 *
 * Uso no núcleo:
 * - VacinacaoService e MedicamentoService gravam calcularFimCarencia(data, carenciaDias);
 *   a carência pode ser 0 (os DTOs aceitam @PositiveOrZero).
 * - VacinacaoConsultaService e MedicamentoConsultaService: carência vigente = fim DEPOIS de hoje.
 * - CarenciaService: getStatusBloqueadosNaCarencia() decide quais saídas são barradas.
 */
public abstract class PoliticaRestricaoSanitariaContrato {

    private static final LocalDate APLICACAO = LocalDate.of(2026, 9, 1);

    protected PoliticaRestricaoSanitaria politica;

    protected abstract PoliticaRestricaoSanitaria criarPolitica();

    @BeforeEach
    public void prepararPolitica() {
        politica = criarPolitica();
    }

    @Test
    @DisplayName("Fim da carência existe e nunca é antes da data de aplicação")
    public void fimNuncaAntesDaAplicacao() {
        for (int dias : new int[] {0, 1, 7, 30, 120}) {
            LocalDate fim = politica.calcularFimCarencia(APLICACAO, dias);

            assertNotNull(fim, "Carência de " + dias + " dias sem data de fim");
            assertFalse(fim.isBefore(APLICACAO), "Carência de " + dias + " dias terminou em " + fim);
        }
    }

    @Test
    @DisplayName("Carência de 0 dias não deixa o animal em carência")
    public void carenciaZeroNaoBloqueia() {
        assertFalse(politica.calcularFimCarencia(APLICACAO, 0).isAfter(APLICACAO));
    }

    @Test
    @DisplayName("Carência positiva deixa o animal em carência depois da aplicação")
    public void carenciaPositivaBloqueia() {
        assertTrue(politica.calcularFimCarencia(APLICACAO, 1).isAfter(APLICACAO));
    }

    @Test
    @DisplayName("Carência maior nunca termina antes de uma carência menor")
    public void carenciaMaiorTerminaDepois() {
        LocalDate fimAnterior = politica.calcularFimCarencia(APLICACAO, 0);

        for (int dias = 1; dias <= 120; dias++) {
            LocalDate fim = politica.calcularFimCarencia(APLICACAO, dias);

            assertFalse(fim.isBefore(fimAnterior),
                    "Carência de " + dias + " dias termina antes da de " + (dias - 1) + " dias");
            fimAnterior = fim;
        }
    }

    @Test
    @DisplayName("Status bloqueados na carência: conjunto definido e sem ATIVO (ATIVO não é uma saída)")
    public void statusBloqueadosValidos() {
        Set<StatusAnimal> bloqueados = politica.getStatusBloqueadosNaCarencia();

        assertNotNull(bloqueados);
        assertFalse(bloqueados.contains(StatusAnimal.ATIVO));
    }
}