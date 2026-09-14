package com.Pecucore.system.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.LoteRequestDTO;
import com.Pecucore.system.model.Lote;
import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.repository.LoteRepository;
import com.Pecucore.system.repository.PropriedadeRepository;
import com.Pecucore.system.model.StatusAnimal;
import com.Pecucore.system.repository.AnimalRepository;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private AnimalRepository animalRepository;

    public Lote create(LoteRequestDTO dados) {

        Propriedade propriedade = propriedadeRepository.findById(dados.propriedadeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Propriedade não encontrada"
                ));

        Lote lote = new Lote();

        lote.setNumero(dados.numero());
        lote.setFinalidade(dados.finalidade());
        lote.setCapacidade(dados.capacidade());
        lote.setDataCriacao(dados.dataCriacao());
        lote.setPropriedade(propriedade);

        return loteRepository.save(lote);
    }

    public Lote update(Long id, LoteRequestDTO dados) {

        Lote loteExistente = loteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lote não encontrado"
                ));

        Propriedade propriedade = propriedadeRepository.findById(dados.propriedadeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Propriedade não encontrada"
                ));

        loteExistente.setNumero(dados.numero());
        loteExistente.setFinalidade(dados.finalidade());
        loteExistente.setCapacidade(dados.capacidade());
        loteExistente.setDataCriacao(dados.dataCriacao());
        loteExistente.setPropriedade(propriedade);

        return loteRepository.save(loteExistente);
    }

    public List<Lote> getAllLotes() {
        return loteRepository.findAll();
    }

    public Lote getLoteById(Long id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lote não encontrado"
                ));
    }

    public void deleteLote(Long id) {

        Lote loteExistente = loteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lote não encontrado"
                ));

        boolean possuiAnimaisAtivos = animalRepository.existsByLoteIdAndStatus(
                id,
                StatusAnimal.ATIVO
        );
        if (possuiAnimaisAtivos){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possivel excluir o lote porque existem animais ativos nesse lote"
            );
        }

        loteRepository.delete(loteExistente);
    }
}