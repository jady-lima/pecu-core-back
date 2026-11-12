package com.pecucore.system.bovinos.agrupamento;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.Lote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetaDesempenhoService {

    @Autowired
    private MetaDesempenhoRepository metaDesempenhoRepository;

    public MetaDesempenho create(MetaDesempenhoRequestDTO dados) {

        if (metaDesempenhoRepository.findByFase(dados.fase()).isPresent()) {
            throw new ConflitoNegocioException(
                    "Já existe uma meta cadastrada para essa fase."
            );
        }

        MetaDesempenho meta = new MetaDesempenho();

        meta.setFase(dados.fase());
        meta.setGmdEsperado(dados.gmdEsperado());

        return metaDesempenhoRepository.save(meta);
    }

    public MetaDesempenho update(Long id, MetaDesempenhoRequestDTO dados) {

        MetaDesempenho meta = getById(id);

        boolean faseEmUso = metaDesempenhoRepository.findByFase(dados.fase())
                .filter(outraMeta -> !outraMeta.getId().equals(id))
                .isPresent();

        if (faseEmUso) {
            throw new ConflitoNegocioException(
                    "Já existe uma meta cadastrada para essa fase."
            );
        }

        meta.setFase(dados.fase());
        meta.setGmdEsperado(dados.gmdEsperado());

        return metaDesempenhoRepository.save(meta);
    }

    public void delete(Long id) {
        MetaDesempenho meta = getById(id);
        metaDesempenhoRepository.delete(meta);
    }

    public MetaDesempenho getById(Long id) {
        return metaDesempenhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Meta de desempenho não encontrada."
                ));
    }

    public List<MetaDesempenho> getAll() {
        return metaDesempenhoRepository.findAll();
    }

    public Optional<Double> getGmdEsperado(Lote lote) {

        if (lote == null) {
            return Optional.empty();
        }

        if (lote.getGmdMeta() != null) {
            return Optional.of(lote.getGmdMeta());
        }

        if (lote.getFinalidade() == null) {
            return Optional.empty();
        }

        return metaDesempenhoRepository.findByFase(lote.getFinalidade())
                .map(MetaDesempenho::getGmdEsperado);
    }
}