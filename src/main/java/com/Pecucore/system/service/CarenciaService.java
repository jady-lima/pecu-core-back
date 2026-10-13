package com.Pecucore.system.service;

import com.Pecucore.system.repository.MedicamentoRepository;
import com.Pecucore.system.repository.VacinacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CarenciaService {

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    public boolean estaEmCarencia(Long animalId) {
        LocalDate hoje = LocalDate.now();

        return vacinacaoRepository.existsByAnimalIdAndDataFimCarenciaAfter(animalId, hoje)
                || medicamentoRepository.existsByAnimalIdAndDataFimCarenciaAfter(animalId, hoje);
    }
}
