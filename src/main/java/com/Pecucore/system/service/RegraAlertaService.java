package com.Pecucore.system.service;

import com.Pecucore.system.model.RegraAlerta;
import com.Pecucore.system.repository.RegraAlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegraAlertaService {

    @Autowired
    private RegraAlertaRepository regraAlertaRepository;

    public RegraAlerta atualizarRegra(
            Long id,
            double parametro,
            boolean ativa
    ) {

        RegraAlerta regra = regraAlertaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra de alerta não encontrada"));

        regra.setParametro(parametro);
        regra.setAtiva(ativa);

        return regraAlertaRepository.save(regra);
    }
}