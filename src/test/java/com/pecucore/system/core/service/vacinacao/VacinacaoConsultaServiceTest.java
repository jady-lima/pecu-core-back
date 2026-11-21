package com.pecucore.system.core.service.vacinacao;

import com.pecucore.system.core.model.Vacina;
import com.pecucore.system.core.model.Vacinacao;
import com.pecucore.system.core.repository.VacinacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VacinacaoConsultaService: vacinação em vigor de cada vacina")
class VacinacaoConsultaServiceTest {

    private static final Long ANIMAL_ID = 7L;

    @Mock
    private VacinacaoRepository vacinacaoRepository;

    @InjectMocks
    private VacinacaoConsultaService vacinacaoConsultaService;

    private static Vacina vacina(Long id) {
        Vacina vacina = mock(Vacina.class);
        lenient().when(vacina.getId()).thenReturn(id);
        return vacina;
    }

    private static Vacinacao vacinacao(Vacina vacina, LocalDate data) {
        Vacinacao vacinacao = new Vacinacao();
        vacinacao.setVacina(vacina);
        vacinacao.setData(data);
        return vacinacao;
    }

    @Test
    @DisplayName("Para cada vacina vale só a última aplicação (dose refeita não conta como vencida)")
    void ultimaAplicacaoDeCadaVacina() {
        Vacina vacinaA = vacina(1L);
        Vacina vacinaB = vacina(2L);
        Vacinacao vacinaA2025 = vacinacao(vacinaA, LocalDate.of(2025, 3, 1));
        Vacinacao vacinaB2025 = vacinacao(vacinaB, LocalDate.of(2025, 6, 1));
        Vacinacao vacinaA2026 = vacinacao(vacinaA, LocalDate.of(2026, 3, 1));

        when(vacinacaoRepository.findByAnimalIdOrderByDataAscIdAsc(ANIMAL_ID))
                .thenReturn(List.of(vacinaA2025, vacinaB2025, vacinaA2026));

        List<Vacinacao> ultimas = vacinacaoConsultaService.getUltimasVacinacoesByAnimalId(ANIMAL_ID);

        assertEquals(2, ultimas.size());
        assertTrue(ultimas.contains(vacinaA2026));
        assertTrue(ultimas.contains(vacinaB2025));
        assertFalse(ultimas.contains(vacinaA2025));
    }

    @Test
    @DisplayName("Animal sem vacinações: lista vazia")
    void semVacinacoes() {
        when(vacinacaoRepository.findByAnimalIdOrderByDataAscIdAsc(ANIMAL_ID)).thenReturn(List.of());

        assertTrue(vacinacaoConsultaService.getUltimasVacinacoesByAnimalId(ANIMAL_ID).isEmpty());
    }
}