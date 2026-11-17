package com.pecucore.system.bovinos.service.agrupamento;

import com.pecucore.system.bovinos.dto.meta.MetaDesempenhoRequestDTO;
import com.pecucore.system.bovinos.model.FinalidadeLote;
import com.pecucore.system.bovinos.model.MetaDesempenho;
import com.pecucore.system.bovinos.repository.MetaDesempenhoRepository;
import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetaDesempenhoService {

    @Autowired
    private MetaDesempenhoRepository metaDesempenhoRepository;

    public MetaDesempenho createMetaDesempenho(MetaDesempenhoRequestDTO dados) {

        if (metaDesempenhoRepository.findByFase(dados.fase()).isPresent()) {
            throw new ConflitoNegocioException("Já existe uma meta cadastrada para essa fase.");
        }

        MetaDesempenho meta = new MetaDesempenho();

        meta.setFase(dados.fase());
        meta.setGmdEsperado(dados.gmdEsperado());

        return metaDesempenhoRepository.save(meta);
    }

    public MetaDesempenho updateMetaDesempenho(Long id, MetaDesempenhoRequestDTO dados) {

        MetaDesempenho meta = getMetaDesempenhoById(id);

        boolean faseEmUso = metaDesempenhoRepository.findByFase(dados.fase())
                .filter(outraMeta -> !outraMeta.getId().equals(id))
                .isPresent();

        if (faseEmUso) {
            throw new ConflitoNegocioException("Já existe uma meta cadastrada para essa fase.");
        }

        meta.setFase(dados.fase());
        meta.setGmdEsperado(dados.gmdEsperado());

        return metaDesempenhoRepository.save(meta);
    }

    public void deleteMetaDesempenho(Long id) {
        MetaDesempenho meta = getMetaDesempenhoById(id);
        metaDesempenhoRepository.delete(meta);
    }

    public MetaDesempenho getMetaDesempenhoById(Long id) {
        return metaDesempenhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Meta de desempenho não encontrada."));
    }

    public List<MetaDesempenho> getAllMetasDesempenho() {
        return metaDesempenhoRepository.findAll();
    }

    public Optional<Double> getGmdEsperadoByFase(FinalidadeLote fase) {
        return metaDesempenhoRepository.findByFase(fase)
                .map(MetaDesempenho::getGmdEsperado);
    }
}