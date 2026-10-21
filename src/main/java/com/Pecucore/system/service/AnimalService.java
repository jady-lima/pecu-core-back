package com.Pecucore.system.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.Pecucore.system.exception.ConflitoNegocioException;
import com.Pecucore.system.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Pecucore.system.dto.AnimalRequestDTO;
import com.Pecucore.system.dto.AnimalStatusRequestDTO;
import com.Pecucore.system.evento.PesoAnimalAlteradoEvento;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Lote;
import com.Pecucore.system.model.StatusAnimal;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.AnimalRepository;
import com.Pecucore.system.repository.LoteRepository;

@Service
public class AnimalService {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CarenciaService carenciaService;

    @Autowired
    private AcessoPropriedadeService acessoPropriedadeService;

    @Transactional
    public Animal create(
            AnimalRequestDTO dados,
            Usuario usuarioLogado
    ) {

        if (animalRepository.existsByBrinco(dados.brinco())) {
            throw new ConflitoNegocioException(
                    "Já existe um animal com este brinco"
            );
        }

        Lote lote =
                loteRepository.findById(dados.loteId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Lote não encontrado"
                        ));

        validarAcessoAoLote(
                lote,
                usuarioLogado
        );

        validarCapacidadeLote(lote);

        Animal animal = new Animal();

        animal.setBrinco(dados.brinco());
        animal.setDataNascimento(dados.dataNascimento());
        animal.setSexo(dados.sexo());
        animal.setLote(lote);
        animal.setStatus(StatusAnimal.ATIVO);

        Animal animalCriado =
                animalRepository.save(animal);

        eventPublisher.publishEvent(
                new PesoAnimalAlteradoEvento(
                        animalCriado.getId(),
                        dados.pesoAtual(),
                        dados.dataPrimeiraPesagem()
                )
        );

        return animalCriado;
    }

    @Transactional
    public Animal update(
            Long id,
            AnimalRequestDTO dados,
            Usuario usuarioLogado
    ) {

        Animal animalExistente =
                getAnimalById(
                        id,
                        usuarioLogado
                );

        if (!Objects.equals(
                animalExistente.getDataNascimento(),
                dados.dataNascimento()
        )) {

            throw new ConflitoNegocioException(
                    "A data de nascimento do animal não pode ser alterada"
            );
        }

        if (animalRepository.existsByBrincoAndIdNot(
                dados.brinco(),
                id
        )) {

            throw new ConflitoNegocioException(
                    "Já existe outro animal com este brinco"
            );
        }

        Lote lote =
                loteRepository.findById(dados.loteId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Lote não encontrado"
                        ));

        validarAcessoAoLote(
                lote,
                usuarioLogado
        );

        boolean mudouDeLote = animalExistente.getLote() == null
                || !animalExistente.getLote().getId().equals(lote.getId());

        if (mudouDeLote && animalExistente.getStatus() == StatusAnimal.ATIVO) {
            validarCapacidadeLote(lote);
        }

        animalExistente.setBrinco(dados.brinco());
        animalExistente.setSexo(dados.sexo());
        animalExistente.setLote(lote);

        Animal animalAtualizado =
                animalRepository.save(animalExistente);

        if (Double.compare(
                animalExistente.getPesoAtual(),
                dados.pesoAtual()
        ) != 0) {

            eventPublisher.publishEvent(
                    new PesoAnimalAlteradoEvento(
                            id,
                            dados.pesoAtual(),
                            LocalDate.now()
                    )
            );
        }

        return animalAtualizado;
    }

    @Transactional
    public Animal updateStatus(
            Long id,
            AnimalStatusRequestDTO dados,
            Usuario usuarioLogado
    ) {

        Animal animal =
                getAnimalById(
                        id,
                        usuarioLogado
                );

        if (animal.getStatus() != StatusAnimal.ATIVO) {

            throw new ConflitoNegocioException(
                    "Não é possível alterar o status de um animal com status " + animal.getStatus()
            );
        }

        if (dados.status() == StatusAnimal.ATIVO) {

            throw new ConflitoNegocioException(
                    "O animal já está com o status ATIVO"
            );
        }

        if ((dados.status() == StatusAnimal.ABATIDO || dados.status() == StatusAnimal.VENDIDO)
                && carenciaService.estaEmCarencia(id)) {

            throw new ConflitoNegocioException(
                    "O animal está em período de carência e não pode ser vendido ou abatido"
            );
        }

        animal.setStatus(dados.status());

        return animalRepository.save(animal);
    }

    @Transactional
    public Animal atualizarPesoAtual(
            Animal animal,
            double pesoKg
    ) {

        animal.setPesoAtual(pesoKg);

        return animalRepository.save(animal);
    }

    public List<Animal> getAllAnimais(
            Usuario usuarioLogado
    ) {

        return acessoPropriedadeService.filtrarPorPerfil(
                usuarioLogado,
                animalRepository::findAll,
                animalRepository::findByLotePropriedadeUsuarioId,
                animalRepository::findByLotePropriedadeVeterinariosId
        );
    }

    public Animal getAnimalById(Long id) {

        return animalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Animal não encontrado"
                ));
    }

    public Animal getAnimalById(
            Long id,
            Usuario usuarioLogado
    ) {

        Animal animal =
                getAnimalById(id);

        validarAcessoAoLote(
                animal.getLote(),
                usuarioLogado
        );

        return animal;
    }

    private Animal getAnimalByBrinco(int brinco) {

        return animalRepository.findByBrinco(brinco)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Animal não encontrado"
                ));
    }

    public Animal getAnimalAtivoById(Long id) {
        return validarAtivo(getAnimalById(id));
    }

    public Animal getAnimalByBrinco(int brinco,Usuario usuarioLogado) {
        Animal animal = getAnimalByBrinco(brinco);

        validarAcessoAoLote(animal.getLote(),usuarioLogado);

        return animal;
    }

    public Animal getAnimalAtivoByBrinco(
            int brinco,
            Usuario usuarioLogado
    ) {
        return validarAtivo(getAnimalByBrinco(brinco, usuarioLogado));
    }

    public List<Animal> getAnimaisAtivosByLoteId(Long loteId) {
        return animalRepository.findByLoteIdAndStatus(
                loteId,
                StatusAnimal.ATIVO
        );
    }

    public long countAnimaisAtivosByLoteId(Long loteId) {
        return animalRepository.countByLoteIdAndStatus(
                loteId,
                StatusAnimal.ATIVO
        );
    }

    public boolean existsAnimalAtivoByLoteId(Long loteId) {
        return animalRepository.existsByLoteIdAndStatus(
                loteId,
                StatusAnimal.ATIVO
        );
    }

    public void validarDataRegistro(
            Animal animal,
            LocalDate data
    ) {

        if (animal.getDataNascimento() != null
                && data.isBefore(animal.getDataNascimento())) {

            throw new ConflitoNegocioException(
                    "A data do registro não pode ser anterior à data de nascimento do animal"
            );
        }
    }

    private Animal validarAtivo(Animal animal) {
        if (animal.getStatus() != StatusAnimal.ATIVO) {

            throw new ConflitoNegocioException(
                    "Não é possível registrar eventos para um animal que não está ativo"
            );
        }

        return animal;
    }

    private void validarCapacidadeLote(Lote lote) {

        long animaisAtivos =
                animalRepository.countByLoteIdAndStatus(
                        lote.getId(),
                        StatusAnimal.ATIVO
                );

        if (animaisAtivos >= lote.getCapacidade()) {

            throw new ConflitoNegocioException(
                    "O lote atingiu a capacidade máxima de " + lote.getCapacidade() + " animais"
            );
        }
    }

    private void validarAcessoAoLote(
            Lote lote,
            Usuario usuarioLogado
    ) {

        acessoPropriedadeService.validarAcesso(
                lote != null ? lote.getPropriedade() : null,
                usuarioLogado,
                "Você não tem acesso a este lote"
        );
    }
}