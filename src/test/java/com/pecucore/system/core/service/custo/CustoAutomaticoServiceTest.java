package com.pecucore.system.core.service.custo;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Custo;
import com.pecucore.system.core.model.OrigemCusto;
import com.pecucore.system.core.model.TipoCusto;
import com.pecucore.system.core.repository.CustoRepository;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustoAutomaticoServiceTest {

    @Test
    void deveAtualizarCustoExistenteSemCriarOutro() {
        CustoAutomaticoService service =
                new CustoAutomaticoService();

        CustoRepository repository =
                mock(CustoRepository.class);

        ReflectionTestUtils.setField(
                service, "custoRepository", repository);

        Custo custo = new Custo();
        custo.setValor(50.0);
        custo.setData(LocalDate.of(2026, 11, 1));

        when(repository.save(any(Custo.class)))
                .thenAnswer(invocacao ->
                        invocacao.getArgument(0));

        Custo resultado = service.sincronizarCustoAutomatico(
                custo,
                TipoCusto.ALIMENTACAO,
                80.0,
                LocalDate.of(2026, 11, 8),
                mock(Animal.class)
        );

        assertSame(custo, resultado);
        assertEquals(80.0, resultado.getValor());
        assertEquals(LocalDate.of(2026, 11, 8),
                resultado.getData());

        verify(repository, times(1)).save(custo);
    }

    @Test
    void deveExcluirCustoQuandoValorRemovido() {
        CustoAutomaticoService service =
                new CustoAutomaticoService();

        CustoRepository repository =
                mock(CustoRepository.class);

        ReflectionTestUtils.setField(
                service, "custoRepository", repository);

        Custo custo = new Custo();

        Custo resultado = service.sincronizarCustoAutomatico(
                custo,
                TipoCusto.ALIMENTACAO,
                null,
                LocalDate.of(2026, 11, 8),
                mock(Animal.class)
        );

        assertNull(resultado);
        verify(repository).delete(custo);
        verify(repository, never()).save(any(Custo.class));
    }

    @Test
    void deveRegistrarNovoCustoAutomaticoQuandoAindaNaoExiste() {
        CustoAutomaticoService service =
                new CustoAutomaticoService();

        CustoRepository repository =
                mock(CustoRepository.class);

        ReflectionTestUtils.setField(
                service, "custoRepository", repository);

        Animal animal = mock(Animal.class);

        when(repository.save(any(Custo.class)))
                .thenAnswer(invocacao ->
                        invocacao.getArgument(0));

        Custo resultado = service.sincronizarCustoAutomatico(
                null,
                TipoCusto.VACINA,
                45.0,
                LocalDate.of(2026, 11, 8),
                animal
        );

        assertNotNull(resultado);
        assertEquals(TipoCusto.VACINA, resultado.getTipo());
        assertEquals(OrigemCusto.AUTOMATICO, resultado.getOrigem());
        assertEquals(45.0, resultado.getValor());
        assertEquals(LocalDate.of(2026, 11, 8), resultado.getData());
        assertSame(animal, resultado.getAnimal());

        verify(repository, times(1)).save(resultado);
    }

    @Test
    void naoDeveFazerNadaSemCustoAtualESemValor() {
        CustoAutomaticoService service =
                new CustoAutomaticoService();

        CustoRepository repository =
                mock(CustoRepository.class);

        ReflectionTestUtils.setField(
                service, "custoRepository", repository);

        Custo resultado = service.sincronizarCustoAutomatico(
                null,
                TipoCusto.ALIMENTACAO,
                null,
                LocalDate.of(2026, 11, 8),
                mock(Animal.class)
        );

        assertNull(resultado);
        verifyNoInteractions(repository);
    }
}