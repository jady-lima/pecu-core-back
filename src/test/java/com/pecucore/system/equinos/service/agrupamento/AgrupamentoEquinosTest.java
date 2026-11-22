package com.pecucore.system.equinos.service.agrupamento;

import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.extensao.desempenho.ResultadoMeta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.StatusAnimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AgrupamentoEquinos: fases, metas padrão e saídas dos equinos")
class AgrupamentoEquinosTest {

    private final AgrupamentoEquinos agrupamento = new AgrupamentoEquinos();

    private static Animal animal(int idadeMeses, String faseDoLote) {
        Lote lote = new Lote();
        lote.setFinalidade(faseDoLote);

        Animal animal = new Animal();
        animal.setDataNascimento(LocalDate.now().minusMonths(idadeMeses));
        animal.setLote(lote);
        return animal;
    }

    @Nested
    @DisplayName("Requisito: metas padrão com fonte citada")
    class MetasPadrao {

        @Test
        @DisplayName("Potro e jovem: os ganhos diários do NRC (2007) ficam DENTRO da faixa padrão")
        void potroEJovemSeguemNrc() {
            Meta potro = agrupamento.getMetaDaFase("POTRO").orElseThrow();
            Meta jovem = agrupamento.getMetaDaFase("JOVEM").orElseThrow();

            assertEquals(ResultadoMeta.DENTRO, potro.avaliar(0.84));
            assertEquals(ResultadoMeta.DENTRO, potro.avaliar(0.72));
            assertEquals(ResultadoMeta.DENTRO, potro.avaliar(0.45));
            assertEquals(ResultadoMeta.DENTRO, jovem.avaliar(0.29));
            assertEquals(ResultadoMeta.DENTRO, jovem.avaliar(0.18));
        }

        @Test
        @DisplayName("Adulto: manutenção do peso; ganhar ou perder muito fica fora da faixa")
        void adultoManutencao() {
            Meta adulto = agrupamento.getMetaDaFase("ADULTO").orElseThrow();

            assertEquals(ResultadoMeta.DENTRO, adulto.avaliar(0.0));
            assertEquals(ResultadoMeta.ACIMA, adulto.avaliar(0.3));
            assertEquals(ResultadoMeta.ABAIXO, adulto.avaliar(-0.3));
        }

        @Test
        @DisplayName("Reprodução e observação não têm meta padrão: o produtor define a do lote")
        void semMetaPadrao() {
            assertTrue(agrupamento.getMetaDaFase("REPRODUCAO").isEmpty());
            assertTrue(agrupamento.getMetaDaFase("OBSERVACAO").isEmpty());
        }
    }

    @Nested
    @DisplayName("Requisito: sugerir a fase pela idade")
    class SugestaoDeFase {

        @Test
        @DisplayName("Até 12 meses é potro, até 36 é jovem e depois é adulto")
        void fasePorIdade() {
            assertEquals(Optional.of("POTRO"), agrupamento.sugerirFase(animal(6, "JOVEM")));
            assertEquals(Optional.of("JOVEM"), agrupamento.sugerirFase(animal(24, "POTRO")));
            assertEquals(Optional.of("ADULTO"), agrupamento.sugerirFase(animal(60, "JOVEM")));
        }

        @Test
        @DisplayName("Lote de reprodução é fase de manejo, não de idade: sem sugestão")
        void faseDeManejoSemSugestao() {
            assertTrue(agrupamento.sugerirFase(animal(60, "REPRODUCAO")).isEmpty());
        }
    }

    @Test
    @DisplayName("Requisito: equinos saem do rebanho por venda ou óbito (abate fora do escopo)")
    void saidas() {
        assertEquals(Set.of(StatusAnimal.VENDIDO, StatusAnimal.OBITO), agrupamento.getSaidasPossiveis());
    }
}