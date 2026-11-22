package com.pecucore.system.equinos.service.sanidade;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.model.Vacina;
import com.pecucore.system.core.model.Vacinacao;
import com.pecucore.system.core.service.vacinacao.VacinaService;
import com.pecucore.system.core.service.vacinacao.VacinacaoConsultaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestricaoSanitariaEquinos: vacinas e exames exigidos para a venda")
class RestricaoSanitariaEquinosTest {

    private static final Long ANIMAL_ID = 7L;
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 22);

    @Mock
    private VacinaService vacinaService;

    @Mock
    private VacinacaoConsultaService vacinacaoConsultaService;

    private RestricaoSanitariaEquinos restricao;
    private Animal animal;
    private Vacina exameAie;
    private Vacina exameMormo;

    @BeforeEach
    void setUp() {
        restricao = new RestricaoSanitariaEquinos(vacinaService, vacinacaoConsultaService);

        animal = mock(Animal.class);
        lenient().when(animal.getId()).thenReturn(ANIMAL_ID);

        exameAie = vacina(1L, "EXAME AIE");
        exameMormo = vacina(2L, "EXAME MORMO");
    }

    private static Vacina vacina(Long id, String nome) {
        Vacina vacina = mock(Vacina.class);
        lenient().when(vacina.getId()).thenReturn(id);
        lenient().when(vacina.getNome()).thenReturn(nome);
        return vacina;
    }

    private static Vacinacao registro(Vacina vacina, LocalDate validoAte) {
        Vacinacao vacinacao = new Vacinacao();
        vacinacao.setVacina(vacina);
        vacinacao.setDataProximaDose(validoAte);
        return vacinacao;
    }

    private void dadosExigidos(Vacina... vacinas) {
        when(vacinaService.getAllVacinasExigidasParaTransito()).thenReturn(List.of(vacinas));
    }

    private void dadosRegistros(Vacinacao... registros) {
        when(vacinacaoConsultaService.getUltimasVacinacoesByAnimalId(ANIMAL_ID)).thenReturn(List.of(registros));
    }

    @Nested
    @DisplayName("Requisito: para vender, os exames exigidos precisam estar em dia")
    class Venda {

        @Test
        @DisplayName("Exame em dia: venda liberada")
        void exameEmDia() {
            dadosExigidos(exameAie);
            dadosRegistros(registro(exameAie, HOJE.plusDays(10)));

            assertTrue(restricao.getRestricoesAdicionais(animal, StatusAnimal.VENDIDO, HOJE).isEmpty());
        }

        @Test
        @DisplayName("No último dia de validade o exame ainda vale")
        void ultimoDiaDeValidade() {
            dadosExigidos(exameAie);
            dadosRegistros(registro(exameAie, HOJE));

            assertTrue(restricao.getRestricoesAdicionais(animal, StatusAnimal.VENDIDO, HOJE).isEmpty());
        }

        @Test
        @DisplayName("Exame vencido: venda bloqueada, com o nome do exame e a data de vencimento")
        void exameVencido() {
            dadosExigidos(exameAie);
            dadosRegistros(registro(exameAie, HOJE.minusDays(1)));

            List<String> restricoes = restricao.getRestricoesAdicionais(animal, StatusAnimal.VENDIDO, HOJE);

            assertEquals(List.of("EXAME AIE: vencido em 21/11/2026"), restricoes);
        }

        @Test
        @DisplayName("Exame exigido sem nenhum registro: venda bloqueada")
        void semRegistro() {
            dadosExigidos(exameAie);
            dadosRegistros();

            assertEquals(List.of("EXAME AIE: sem registro"),
                    restricao.getRestricoesAdicionais(animal, StatusAnimal.VENDIDO, HOJE));
        }

        @Test
        @DisplayName("Com vários exames exigidos, só os pendentes aparecem")
        void variosExames() {
            dadosExigidos(exameAie, exameMormo);
            dadosRegistros(registro(exameAie, HOJE.plusDays(30)));

            assertEquals(List.of("EXAME MORMO: sem registro"),
                    restricao.getRestricoesAdicionais(animal, StatusAnimal.VENDIDO, HOJE));
        }
    }

    @Nested
    @DisplayName("Requisito: a exigência vale só para a venda e só para o que foi marcado")
    class Abrangencia {

        @Test
        @DisplayName("Óbito não exige exames: nem consulta os registros")
        void obitoNaoExigeExames() {
            assertTrue(restricao.getRestricoesAdicionais(animal, StatusAnimal.OBITO, HOJE).isEmpty());
            verifyNoInteractions(vacinaService, vacinacaoConsultaService);
        }

        @Test
        @DisplayName("Sem vacinas ou exames marcados como exigidos: nenhuma restrição")
        void nadaExigido() {
            dadosExigidos();

            assertTrue(restricao.getRestricoesAdicionais(animal, StatusAnimal.VENDIDO, HOJE).isEmpty());
            verifyNoInteractions(vacinacaoConsultaService);
        }
    }
}