package com.Pecucore.system.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.AnimalRequestDTO;
import com.Pecucore.system.dto.AnimalStatusRequestDTO;
import com.Pecucore.system.evento.PesoAnimalAlteradoEvento;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Lote;
import com.Pecucore.system.model.Perfil;
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
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um animal com este brinco"
            );
        }

        Lote lote =
                loteRepository.findById(dados.loteId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Lote não encontrado"
                        ));

        validarAcessoAoLote(
                lote,
                usuarioLogado
        );

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
                        LocalDate.now()
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

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A data de nascimento do animal não pode ser alterada"
            );
        }

        if (animalRepository.existsByBrincoAndIdNot(
                dados.brinco(),
                id
        )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe outro animal com este brinco"
            );
        }

        Lote lote =
                loteRepository.findById(dados.loteId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Lote não encontrado"
                        ));

        validarAcessoAoLote(
                lote,
                usuarioLogado
        );

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

        if (dados.status() == StatusAnimal.ABATIDO
                && carenciaService.estaEmCarencia(id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O animal está em período de carência e não pode ser abatido"
            );
        }

        animal.setStatus(dados.status());

        return animalRepository.save(animal);
    }

    public List<Animal> getAllAnimais(
            Usuario usuarioLogado
    ) {

        if (usuarioLogado.getPerfil() == Perfil.PRODUTOR) {

            return animalRepository
                    .findByLotePropriedadeUsuarioId(
                            usuarioLogado.getId()
                    );
        }

        if (usuarioLogado.getPerfil() == Perfil.VETERINARIO) {
            return animalRepository.findByLotePropriedadeVeterinariosId(usuarioLogado.getId());
        }

        return animalRepository.findAll();
    }

    public Animal getAnimalById(Long id) {

        return animalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
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

    public Animal getAnimalByBrinco(int brinco) {

        return animalRepository.findByBrinco(brinco)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Animal não encontrado"
                ));
    }

    public Animal getAnimalAtivoById(Long id) {
        return validarAtivo(getAnimalById(id));
    }

    public Animal getAnimalAtivoByBrinco(int brinco) {
        return validarAtivo(getAnimalByBrinco(brinco));
    }

    public List<Animal> getAnimaisAtivosByLoteId(Long loteId) {
        return animalRepository.findByLoteIdAndStatus(
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

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A data do registro não pode ser anterior à data de nascimento do animal"
            );
        }
    }

    private Animal validarAtivo(Animal animal) {

        if (animal.getStatus() != StatusAnimal.ATIVO) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível registrar eventos para um animal que não está ativo"
            );
        }

        return animal;
    }

    private void validarAcessoAoLote(
            Lote lote,
            Usuario usuarioLogado
    ) {

        if (!acessoPropriedadeService.possuiAcesso(lote != null ? lote.getPropriedade() : null, usuarioLogado)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem acesso a este lote"
            );
        }
    }

    public void deleteAnimal(
            Long id,
            Usuario usuarioLogado
    ) {

        getAnimalById(
                id,
                usuarioLogado
        );

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Não é possível excluir um animal, pois o histórico deve ser mantido"
        );
    }
}