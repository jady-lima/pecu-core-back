package com.pecucore.system.core.service.lote;

import com.pecucore.system.core.dto.meta.MetaFaseRequestDTO;
import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.extensao.agrupamento.FaseLote;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.model.MetaFase;
import com.pecucore.system.core.repository.MetaFaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Metas de desempenho cadastradas por fase (frozen spot). As fases válidas vêm da aplicação ativa
 * (PoliticaAgrupamento); a meta padrão da aplicação é usada pelo LoteService quando não há meta cadastrada.
 */
@Service
public class MetaFaseService {

    @Autowired
    private MetaFaseRepository metaFaseRepository;

    @Autowired
    private PoliticaAgrupamento politicaAgrupamento;

    public MetaFase createMetaFase(MetaFaseRequestDTO dados) {
        validarFase(dados.fase());
        Meta meta = montarMeta(dados);

        if (metaFaseRepository.findByFase(dados.fase()).isPresent()) {
            throw new ConflitoNegocioException("Já existe uma meta cadastrada para essa fase.");
        }

        MetaFase metaFase = new MetaFase();
        preencherMetaFase(metaFase, dados.fase(), meta);

        return metaFaseRepository.save(metaFase);
    }

    public MetaFase updateMetaFase(Long id, MetaFaseRequestDTO dados) {
        MetaFase metaFase = getMetaFaseById(id);

        validarFase(dados.fase());
        Meta meta = montarMeta(dados);

        boolean faseEmUso = metaFaseRepository.findByFase(dados.fase())
                .filter(outraMeta -> !id.equals(outraMeta.getId()))
                .isPresent();

        if (faseEmUso) {
            throw new ConflitoNegocioException("Já existe uma meta cadastrada para essa fase.");
        }

        preencherMetaFase(metaFase, dados.fase(), meta);

        return metaFaseRepository.save(metaFase);
    }

    public void deleteMetaFase(Long id) {
        MetaFase metaFase = getMetaFaseById(id);
        metaFaseRepository.delete(metaFase);
    }

    public MetaFase getMetaFaseById(Long id) {
        return metaFaseRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Meta de desempenho não encontrada."));
    }

    public List<MetaFase> getAllMetasFase() {
        return metaFaseRepository.findAll();
    }

    /** Meta cadastrada para a fase; vazio quando não há nenhuma cadastrada. */
    public Optional<Meta> getMetaByFase(String codigoFase) {
        return metaFaseRepository.findByFase(codigoFase)
                .map(metaFase -> new Meta(metaFase.getMinimo(), metaFase.getMaximo()));
    }

    private void validarFase(String fase) {
        List<String> codigos = politicaAgrupamento.getFases().stream()
                .map(FaseLote::codigo)
                .toList();

        if (!codigos.contains(fase)) {
            throw new RegraNegocioException(
                    "Fase inválida: " + fase + ". Valores aceitos: " + String.join(", ", codigos) + "."
            );
        }
    }

    private Meta montarMeta(MetaFaseRequestDTO dados) {
        try {
            return new Meta(dados.minimo(), dados.maximo());
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(e.getMessage());
        }
    }

    private void preencherMetaFase(MetaFase metaFase, String fase, Meta meta) {
        metaFase.setFase(fase);
        metaFase.setMinimo(meta.minimo());
        metaFase.setMaximo(meta.maximo());
    }
}