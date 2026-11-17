package com.pecucore.system.core.service;

import com.pecucore.system.core.repository.VacinacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VacinacaoConsultaService {

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    public boolean existsVacinacaoByVacinaId(Long vacinaId) {
        return vacinacaoRepository.existsByVacinaId(vacinaId);
    }
}
