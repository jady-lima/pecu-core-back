package com.pecucore.system.core.extensao.agrupamento;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.StatusAnimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contrato do ponto flexível PoliticaAgrupamento: o que o núcleo espera de qualquer implementação.
 *
 * Uso no núcleo:
 * - LoteService: getFases() define as finalidades aceitas para o lote; getMetaDaFase() é a meta
 *   usada quando o lote não tem meta própria.
 * - ReagrupamentoService: sugerirFase() é comparada com a finalidade dos lotes para sugerir a mudança.
 */
public abstract class PoliticaAgrupamentoContrato {

    private static final int[] IDADES_MESES = {0, 3, 6, 9, 12, 18, 24, 36, 60, 120};

    protected PoliticaAgrupamento politica;

    protected abstract PoliticaAgrupamento criarPolitica();

    @BeforeEach
    public void prepararPolitica() {
        politica = criarPolitica();
    }

    private List<String> codigos() {
        return politica.getFases().stream().map(FaseLote::codigo).toList();
    }

    private static Animal animal(LocalDate nascimento, Lote lote) {
        Animal animal = new Animal();
        animal.setDataNascimento(nascimento);
        animal.setLote(lote);
        return animal;
    }

    private static Lote lote(String finalidade) {
        Lote lote = new Lote();
        lote.setFinalidade(finalidade);
        return lote;
    }

    @Test
    @DisplayName("Existe pelo menos uma fase, com código e descrição preenchidos")
    public void fasesPreenchidas() {
        List<FaseLote> fases = politica.getFases();

        assertFalse(fases == null || fases.isEmpty(), "A política não tem fases");
        for (FaseLote fase : fases) {
            assertFalse(fase.codigo() == null || fase.codigo().isBlank(), "Fase sem código: " + fase);
            assertFalse(fase.descricao() == null || fase.descricao().isBlank(), "Fase sem descrição: " + fase);
        }
    }

    @Test
    @DisplayName("Códigos das fases não se repetem (são gravados como finalidade do lote)")
    public void codigosUnicos() {
        List<String> codigos = codigos();

        assertEquals(codigos.size(), new HashSet<>(codigos).size(), "Códigos repetidos: " + codigos);
    }

    @Test
    @DisplayName("Fase desconhecida não tem meta (vazio, sem erro)")
    public void faseDesconhecidaSemMeta() {
        assertTrue(politica.getMetaDaFase("FASE_INEXISTENTE_NO_TESTE").isEmpty());
    }

    @Test
    @DisplayName("Consultar a meta de qualquer fase conhecida não gera erro")
    public void metaDeFaseConhecida() {
        for (String codigo : codigos()) {
            assertDoesNotThrow(() -> assertNotNull(politica.getMetaDaFase(codigo)), "Fase " + codigo);
        }
    }

    @Test
    @DisplayName("Sugestão de fase, quando existe, é sempre uma das fases da política")
    public void sugestaoEhFaseValida() {
        List<String> codigos = codigos();

        for (String finalidadeAtual : codigos) {
            for (int meses : IDADES_MESES) {
                Animal animal = animal(LocalDate.now().minusMonths(meses), lote(finalidadeAtual));
                Optional<String> sugestao = politica.sugerirFase(animal);

                assertNotNull(sugestao);
                sugestao.ifPresent(fase -> assertTrue(codigos.contains(fase),
                        "Fase sugerida desconhecida: " + fase + " (animal de " + meses + " meses em " + finalidadeAtual + ")"));
            }
        }
    }

    @Test
    @DisplayName("Animal sem lote ou sem data de nascimento não gera erro na sugestão")
    public void sugestaoSemDadosNaoGeraErro() {
        List<Animal> animais = new ArrayList<>();
        animais.add(animal(LocalDate.now().minusMonths(6), null));
        animais.add(animal(null, lote(codigos().get(0))));
        animais.add(animal(null, null));

        for (Animal animal : animais) {
            assertDoesNotThrow(() -> assertNotNull(politica.sugerirFase(animal)));
        }
    }


    @Test
    @DisplayName("Saídas do rebanho: a aplicação prevê pelo menos uma, e nunca ATIVO")
    public void saidasPossiveisValidas() {
        Set<StatusAnimal> saidas = politica.getSaidasPossiveis();

        assertFalse(saidas == null || saidas.isEmpty(), "A aplicação não prevê nenhuma saída do rebanho");
        assertFalse(saidas.contains(StatusAnimal.ATIVO), "ATIVO não é uma saída do rebanho");
    }
}