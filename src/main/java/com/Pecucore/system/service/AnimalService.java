package com.Pecucore.system.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.AnimalRequestDTO;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.StatusAnimal;
import com.Pecucore.system.model.Lote;
import com.Pecucore.system.repository.AnimalRepository;
import com.Pecucore.system.repository.LoteRepository;

@Service
public class AnimalService {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private LoteRepository loteRepository;

    public Animal create(AnimalRequestDTO dados) {

        if (animalRepository.existsByBrinco(dados.brinco())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um animal com este brinco"
            );
        }

        Lote lote = loteRepository.findById(dados.loteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lote não encontrado"
                ));

        Animal animal = new Animal();

        animal.setBrinco(dados.brinco());
        animal.setDataNascimento(dados.dataNascimento());
        animal.setPesoInicial(dados.pesoInicial());
        animal.setSexo(dados.sexo());
        animal.setLote(lote);
        animal.setStatus(StatusAnimal.ATIVO);

        return animalRepository.save(animal);
    }

    public Animal update(Long id, AnimalRequestDTO dados) {

        Animal animalExistente = animalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Animal não encontrado"
                ));


        if (animalRepository.existsByBrincoAndIdNot(dados.brinco(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe outro animal com este brinco"
            );
        }

        Lote lote = loteRepository.findById(dados.loteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lote não encontrado"
                ));

        animalExistente.setBrinco(dados.brinco());
        animalExistente.setDataNascimento(dados.dataNascimento());
        animalExistente.setPesoInicial(dados.pesoInicial());
        animalExistente.setSexo(dados.sexo());
        animalExistente.setLote(lote);

        return animalRepository.save(animalExistente);
    }

    public List<Animal> getAllAnimais() {
        return animalRepository.findAll();
    }

    public Animal getAnimalById(Long id) {

        return animalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Animal não encontrado"
                ));
    }

    public void deleteAnimal(Long id) {

        Animal animalExistente = animalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Animal não encontrado"
                ));

        animalRepository.delete(animalExistente);
    }
}