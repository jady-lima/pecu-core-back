package com.Pecucore.system.service;

import com.Pecucore.system.model.Lote;
import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.repository.LoteRepository;
import java.util.List;

import com.Pecucore.system.repository.PropriedadeRepository;
import org.springframework.stereotype.Service;

@Service
public class LoteService {

    private final LoteRepository loteRepository;
    private final PropriedadeRepository propriedadeRepository;

    public LoteService(LoteRepository loteRepository,
    PropriedadeRepository propriedadeRepository) {
        this.loteRepository = loteRepository;
        this.propriedadeRepository = propriedadeRepository;
    }
//Exceção de so poder cadastrar com uma propriedade
    public Lote create(Lote lote) {
        Long propriedadeId = lote.getPropriedade().getId();

        Propriedade propriedade =
                propriedadeRepository.findById(propriedadeId).orElse(null);

        if (propriedade == null) {
            throw new RuntimeException(
                    "Sem Propriedade Cadastrada!! Cadastre uma Propriedade."
            );
        }
        if (lote.getCapacidade() <= 0) {
            throw new RuntimeException(
                    "A capacidade do lote deve ser maior que zero."
            );
        }
        return loteRepository.save(lote);
    }

    public List<Lote> findAll() {
        return loteRepository.findAll();
    }

    public Lote findById(Long id) {
        return loteRepository.findById(id).orElse(null);
    }

    public Lote update(Long id, Lote novoLote) {

        Lote lote = loteRepository.findById(id).orElse(null);

        if (lote == null) {
            return null;
        }

        lote.setNumero(novoLote.getNumero());
        lote.setFinalidade(novoLote.getFinalidade());
        lote.setCapacidade(novoLote.getCapacidade());
        lote.setDataCriacao(novoLote.getDataCriacao());
        lote.setPropriedade(novoLote.getPropriedade());

        return loteRepository.save(lote);
    }

    public void delete(Long id) {
        loteRepository.deleteById(id);
    }
}