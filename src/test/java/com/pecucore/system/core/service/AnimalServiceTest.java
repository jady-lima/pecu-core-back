package com.pecucore.system.core.service;

import com.pecucore.system.bovinos.sanidade.CarenciaService;
import com.pecucore.system.core.dto.animal.AnimalRequestDTO;
import com.pecucore.system.core.dto.animal.AnimalStatusRequestDTO;
import com.pecucore.system.core.evento.PesoAnimalAlteradoEvento;
import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.AnimalRepository;
import com.pecucore.system.core.repository.LoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    private static final Long ANIMAL_ID = 2L;
    private static final Long LOTE_ID = 5L;
    private static final int BRINCO = 123;
    private static final int CAPACIDADE_DO_LOTE = 10;
    private static final LocalDate NASCIMENTO = LocalDate.of(2025, 3, 10);
    private static final LocalDate PRIMEIRA_PESAGEM = LocalDate.of(2025, 3, 12);

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CarenciaService carenciaService;

    @Mock
    private AcessoPropriedadeService acessoPropriedadeService;

    @InjectMocks
    private AnimalService animalService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = mock(Usuario.class);
    }

    // ---------- Mudança de status e carência ----------

    @ParameterizedTest(name = "animal em carencia nao pode ficar {0}")
    @EnumSource(value = StatusAnimal.class, names = {"VENDIDO", "ABATIDO"})
    void naoDeveVenderNemAbaterAnimalEmCarencia(StatusAnimal novoStatus) {
        Animal animal = animalCadastrado(StatusAnimal.ATIVO);
        when(carenciaService.estaEmCarencia(ANIMAL_ID)).thenReturn(true);

        assertThrows(ConflitoNegocioException.class, () -> alterarStatusPara(novoStatus));

        assertEquals(StatusAnimal.ATIVO, animal.getStatus());
        verify(animalRepository, never()).save(any());
    }

    @ParameterizedTest(name = "animal fora da carencia pode ficar {0}")
    @EnumSource(value = StatusAnimal.class, names = {"VENDIDO", "ABATIDO"})
    void deveVenderOuAbaterAnimalForaDaCarencia(StatusAnimal novoStatus) {
        animalCadastrado(StatusAnimal.ATIVO);
        when(carenciaService.estaEmCarencia(ANIMAL_ID)).thenReturn(false);
        saveDevolveOProprioAnimal();

        Animal atualizado = alterarStatusPara(novoStatus);

        assertEquals(novoStatus, atualizado.getStatus());
    }

    @Test
    void deveRegistrarObitoSemConsultarCarencia() {
        animalCadastrado(StatusAnimal.ATIVO);
        saveDevolveOProprioAnimal();

        Animal atualizado = alterarStatusPara(StatusAnimal.OBITO);

        assertEquals(StatusAnimal.OBITO, atualizado.getStatus());
        verifyNoInteractions(carenciaService);
    }

    @Test
    void naoDeveAlterarStatusDeAnimalQueJaSaiuDoRebanho() {
        animalCadastrado(StatusAnimal.VENDIDO);

        assertThrows(ConflitoNegocioException.class,
                () -> alterarStatusPara(StatusAnimal.OBITO));

        verify(animalRepository, never()).save(any());
    }

    @Test
    void naoDeveMarcarComoAtivoUmAnimalJaAtivo() {
        animalCadastrado(StatusAnimal.ATIVO);

        assertThrows(ConflitoNegocioException.class,
                () -> alterarStatusPara(StatusAnimal.ATIVO));

        verify(animalRepository, never()).save(any());
    }

    // ---------- Animal inativo não recebe registros ----------

    @Test
    void animalInativoNaoRecebeRegistros() {
        Animal animal = new Animal();
        animal.setStatus(StatusAnimal.ABATIDO);
        when(animalRepository.findByBrinco(BRINCO)).thenReturn(Optional.of(animal));

        assertThrows(ConflitoNegocioException.class,
                () -> animalService.getAnimalAtivoByBrinco(BRINCO, usuario));
    }

    @Test
    void animalAtivoPodeReceberRegistros() {
        Animal animal = new Animal();
        animal.setStatus(StatusAnimal.ATIVO);
        when(animalRepository.findByBrinco(BRINCO)).thenReturn(Optional.of(animal));

        assertSame(animal, animalService.getAnimalAtivoByBrinco(BRINCO, usuario));
    }

    @Test
    void naoDeveAceitarRegistroAnteriorAoNascimento() {
        Animal animal = new Animal();
        animal.setDataNascimento(NASCIMENTO);

        assertThrows(ConflitoNegocioException.class,
                () -> animalService.validarDataRegistro(animal, NASCIMENTO.minusDays(1)));
    }

    @Test
    void deveAceitarRegistroNoDiaDoNascimento() {
        Animal animal = new Animal();
        animal.setDataNascimento(NASCIMENTO);

        assertDoesNotThrow(() -> animalService.validarDataRegistro(animal, NASCIMENTO));
    }

    // ---------- Cadastro ----------

    @Test
    void deveCadastrarAnimalAtivoERegistrarAPrimeiraPesagem() {
        Lote lote = loteComAnimaisAtivos(3);
        saveDevolveOProprioAnimal();

        Animal criado = animalService.createAnimal(dadosDeCadastro(), usuario);

        assertEquals(BRINCO, criado.getBrinco());
        assertEquals(StatusAnimal.ATIVO, criado.getStatus());
        assertSame(lote, criado.getLote());

        ArgumentCaptor<Object> evento = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(evento.capture());

        PesoAnimalAlteradoEvento pesagem = (PesoAnimalAlteradoEvento) evento.getValue();
        assertEquals(300.0, pesagem.pesoKg(), 0.0001);
        assertEquals(PRIMEIRA_PESAGEM, pesagem.data());
    }

    @Test
    void naoDeveCadastrarAnimalEmLoteCheio() {
        loteComAnimaisAtivos(CAPACIDADE_DO_LOTE);

        assertThrows(ConflitoNegocioException.class,
                () -> animalService.createAnimal(dadosDeCadastro(), usuario));

        verify(animalRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void naoDeveCadastrarBrincoDuplicado() {
        when(animalRepository.existsByBrinco(BRINCO)).thenReturn(true);

        assertThrows(ConflitoNegocioException.class,
                () -> animalService.createAnimal(dadosDeCadastro(), usuario));

        verifyNoInteractions(loteRepository);
        verify(animalRepository, never()).save(any());
    }

    // ---------- Auxiliares ----------

    private Animal animalCadastrado(StatusAnimal status) {
        Animal animal = new Animal();
        animal.setStatus(status);

        when(animalRepository.findById(ANIMAL_ID)).thenReturn(Optional.of(animal));

        return animal;
    }

    private Animal alterarStatusPara(StatusAnimal novoStatus) {
        return animalService.updateStatusAnimal(
                ANIMAL_ID, new AnimalStatusRequestDTO(novoStatus), usuario);
    }

    private void saveDevolveOProprioAnimal() {
        when(animalRepository.save(any(Animal.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private Lote loteComAnimaisAtivos(long animaisAtivos) {
        Lote lote = mock(Lote.class);
        when(lote.getId()).thenReturn(LOTE_ID);
        when(lote.getCapacidade()).thenReturn(CAPACIDADE_DO_LOTE);

        when(loteRepository.findById(LOTE_ID)).thenReturn(Optional.of(lote));
        when(animalRepository.countByLoteIdAndStatus(LOTE_ID, StatusAnimal.ATIVO))
                .thenReturn(animaisAtivos);

        return lote;
    }

    private AnimalRequestDTO dadosDeCadastro() {
        return new AnimalRequestDTO(
                BRINCO, NASCIMENTO, 300.0, "M", LOTE_ID, PRIMEIRA_PESAGEM);
    }
}