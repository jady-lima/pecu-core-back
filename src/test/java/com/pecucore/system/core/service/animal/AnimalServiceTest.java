package com.pecucore.system.core.service.animal;

import com.pecucore.system.core.dto.animal.AnimalRequestDTO;
import com.pecucore.system.core.dto.animal.AnimalStatusRequestDTO;
import com.pecucore.system.core.evento.PesoAnimalAlteradoEvento;
import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.AnimalRepository;
import com.pecucore.system.core.service.lote.LoteService;
import com.pecucore.system.core.service.propriedade.AcessoPropriedadeService;
import com.pecucore.system.core.service.sanidade.CarenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Regressão do AnimalService (núcleo): cadastro e ciclo de vida (status) do animal.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnimalService: cadastro e mudança de status do animal")
class AnimalServiceTest {

    private static final Long ANIMAL_ID = 1L;
    private static final Long LOTE_ID = 10L;
    private static final int BRINCO = 101;
    private static final LocalDate NASCIMENTO = LocalDate.of(2025, 3, 10);
    private static final LocalDate PRIMEIRA_PESAGEM = LocalDate.of(2026, 11, 1);

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private LoteService loteService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CarenciaService carenciaService;

    @Mock
    private AcessoPropriedadeService acessoPropriedadeService;

    @Mock
    private PoliticaAgrupamento politicaAgrupamento;

    @InjectMocks
    private AnimalService animalService;

    private Usuario usuario;
    private Lote lote;

    @BeforeEach
    void setUp() {
        usuario = mock(Usuario.class);

        lote = mock(Lote.class);
        lenient().when(lote.getId()).thenReturn(LOTE_ID);
        lenient().when(lote.getCapacidade()).thenReturn(10);

        lenient().when(animalRepository.save(any(Animal.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));


        // Saídas da aplicação simulada (as mesmas dos bovinos, para os testes existentes não mudarem).
        lenient().when(politicaAgrupamento.getSaidasPossiveis())
                .thenReturn(EnumSet.of(StatusAnimal.VENDIDO, StatusAnimal.OBITO, StatusAnimal.ABATIDO));
    }

    private AnimalRequestDTO dadosAnimal(String sexo) {
        return new AnimalRequestDTO(BRINCO, NASCIMENTO, 350.0, sexo, LOTE_ID, PRIMEIRA_PESAGEM);
    }

    private Animal animalCadastrado(StatusAnimal status) {
        Animal animal = new Animal();
        animal.setBrinco(BRINCO);
        animal.setLote(lote);
        animal.setStatus(status);
        when(animalRepository.findById(ANIMAL_ID)).thenReturn(Optional.of(animal));
        return animal;
    }

    @Nested
    @DisplayName("Requisito: cadastrar animal em um lote")
    class Cadastro {

        @Test
        @DisplayName("Animal novo entra ATIVO, com sexo padronizado, e o peso inicial vira a primeira pesagem")
        void cadastraAnimalAtivo() {
            when(loteService.getLoteById(LOTE_ID, usuario)).thenReturn(lote);
            when(animalRepository.countByLoteIdAndStatus(LOTE_ID, StatusAnimal.ATIVO)).thenReturn(3L);

            Animal animal = animalService.createAnimal(dadosAnimal(" m "), usuario);

            assertEquals(StatusAnimal.ATIVO, animal.getStatus());
            assertEquals("M", animal.getSexo());
            assertEquals(NASCIMENTO, animal.getDataNascimento());
            assertSame(lote, animal.getLote());
            verify(eventPublisher).publishEvent(
                    new PesoAnimalAlteradoEvento(null, 350.0, PRIMEIRA_PESAGEM));
        }

        @Test
        @DisplayName("Brinco repetido: cadastro recusado")
        void brincoDuplicado() {
            when(animalRepository.existsByBrinco(BRINCO)).thenReturn(true);

            assertThrows(ConflitoNegocioException.class,
                    () -> animalService.createAnimal(dadosAnimal("F"), usuario));
            verify(animalRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("Lote na capacidade máxima: cadastro recusado")
        void loteLotado() {
            when(loteService.getLoteById(LOTE_ID, usuario)).thenReturn(lote);
            when(animalRepository.countByLoteIdAndStatus(LOTE_ID, StatusAnimal.ATIVO)).thenReturn(10L);

            ConflitoNegocioException erro = assertThrows(ConflitoNegocioException.class,
                    () -> animalService.createAnimal(dadosAnimal("F"), usuario));
            assertTrue(erro.getMessage().contains("10"));
            verify(animalRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Requisito: mudar o status do animal (saída do rebanho)")
    class MudancaDeStatus {

        @Test
        @DisplayName("Animal ATIVO pode sair do rebanho, depois da verificação de carência")
        void ativoParaVendido() {
            animalCadastrado(StatusAnimal.ATIVO);

            Animal animal = animalService.updateStatusAnimal(
                    ANIMAL_ID, new AnimalStatusRequestDTO(StatusAnimal.VENDIDO), usuario);

            assertEquals(StatusAnimal.VENDIDO, animal.getStatus());
            verify(carenciaService).validarMudancaStatus(ANIMAL_ID, StatusAnimal.VENDIDO);
        }

        @Test
        @DisplayName("Animal que já saiu do rebanho não muda mais de status")
        void inativoNaoMuda() {
            animalCadastrado(StatusAnimal.OBITO);

            assertThrows(ConflitoNegocioException.class, () -> animalService.updateStatusAnimal(
                    ANIMAL_ID, new AnimalStatusRequestDTO(StatusAnimal.VENDIDO), usuario));
            verifyNoInteractions(carenciaService);
            verify(animalRepository, never()).save(any());
        }

        @Test
        @DisplayName("ATIVO não é aceito como novo status")
        void naoAceitaAtivo() {
            animalCadastrado(StatusAnimal.ATIVO);

            assertThrows(ConflitoNegocioException.class, () -> animalService.updateStatusAnimal(
                    ANIMAL_ID, new AnimalStatusRequestDTO(StatusAnimal.ATIVO), usuario));
            verifyNoInteractions(carenciaService);
        }

        @Test
        @DisplayName("Carência bloqueando a saída: status não muda e nada é salvo")
        void carenciaBloqueia() {
            Animal animal = animalCadastrado(StatusAnimal.ATIVO);
            doThrow(new ConflitoNegocioException("Animal em carência"))
                    .when(carenciaService).validarMudancaStatus(ANIMAL_ID, StatusAnimal.ABATIDO);

            assertThrows(ConflitoNegocioException.class, () -> animalService.updateStatusAnimal(
                    ANIMAL_ID, new AnimalStatusRequestDTO(StatusAnimal.ABATIDO), usuario));
            assertEquals(StatusAnimal.ATIVO, animal.getStatus());
            verify(animalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Saída que a aplicação não prevê é recusada, sem consultar a carência")
        void saidaNaoPrevistaPelaAplicacao() {
            animalCadastrado(StatusAnimal.ATIVO);
            when(politicaAgrupamento.getSaidasPossiveis())
                    .thenReturn(EnumSet.of(StatusAnimal.VENDIDO, StatusAnimal.OBITO));

            RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                    () -> animalService.updateStatusAnimal(
                            ANIMAL_ID, new AnimalStatusRequestDTO(StatusAnimal.ABATIDO), usuario));

            assertTrue(erro.getMessage().contains("VENDIDO, OBITO"));
            verifyNoInteractions(carenciaService);
            verify(animalRepository, never()).save(any());
        }

        @Test
        @DisplayName("A lista de saídas vem da política da aplicação, em ordem fixa")
        void listaDeSaidasVemDaPolitica() {
            assertEquals(List.of(StatusAnimal.VENDIDO, StatusAnimal.OBITO, StatusAnimal.ABATIDO),
                    animalService.getAllSaidasAnimal());
        }
    }

    @Nested
    @DisplayName("Requisito: animal fora do rebanho não recebe novos registros")
    class RegistrosDeAnimalInativo {

        @Test
        @DisplayName("Buscar animal ativo pelo brinco falha se ele não está ATIVO")
        void animalInativo() {
            Animal animal = new Animal();
            animal.setBrinco(BRINCO);
            animal.setLote(lote);
            animal.setStatus(StatusAnimal.OBITO);
            when(animalRepository.findByBrinco(BRINCO)).thenReturn(Optional.of(animal));

            assertThrows(ConflitoNegocioException.class,
                    () -> animalService.getAnimalAtivoByBrinco(BRINCO, usuario));
        }
    }
}