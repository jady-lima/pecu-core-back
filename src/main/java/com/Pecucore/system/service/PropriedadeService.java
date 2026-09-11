package com.Pecucore.system.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.PropriedadeRequestDTO;
import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.repository.PropriedadeRepository;

@Service
public class PropriedadeService {

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    public Propriedade create(PropriedadeRequestDTO dados) {

        Propriedade propriedade = new Propriedade();

        propriedade.setNome(dados.nome());
        propriedade.setLocalizacao(dados.localizacao());

        return propriedadeRepository.save(propriedade);
    }

    public Propriedade update(Long id, PropriedadeRequestDTO dados) {

        Propriedade propriedadeExistente =
                propriedadeRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Propriedade não encontrada"
                        ));

        propriedadeExistente.setNome(dados.nome());
        propriedadeExistente.setLocalizacao(dados.localizacao());

        return propriedadeRepository.save(propriedadeExistente);
    }

    public List<Propriedade> getAllPropriedades() {
        return propriedadeRepository.findAll();
    }

    public Propriedade getPropriedadeById(Long id) {
        return propriedadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Propriedade não encontrada"
                ));
    }

    public void deletePropriedade(Long id) {

        Propriedade propriedadeExistente =
                propriedadeRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Propriedade não encontrada"
                        ));

        propriedadeRepository.delete(propriedadeExistente);
    }
}