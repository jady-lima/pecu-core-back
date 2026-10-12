package com.Pecucore.system.service;

import com.Pecucore.system.dto.MetaDesempenhoRequestDTO;
import com.Pecucore.system.model.MetaDesempenho;
import com.Pecucore.system.repository.MetaDesempenhoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MetaDesempenhoService {

    @Autowired
    private MetaDesempenhoRepository metaDesempenhoRepository;

    public MetaDesempenho create(MetaDesempenhoRequestDTO dados) {

        if (metaDesempenhoRepository.findByFase(dados.fase()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma meta cadastrada para essa fase."
            );
        }

        MetaDesempenho meta = new MetaDesempenho();

        meta.setFase(dados.fase());
        meta.setGmdEsperado(dados.gmdEsperado());

        return metaDesempenhoRepository.save(meta);
    }

    public List<MetaDesempenho> getAll() {
        return metaDesempenhoRepository.findAll();
    }

    public MetaDesempenho getByFase(com.Pecucore.system.model.FinalidadeLote fase) {

        return metaDesempenhoRepository.findByFase(fase)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meta de desempenho não encontrada para essa fase."
                ));
    }
}