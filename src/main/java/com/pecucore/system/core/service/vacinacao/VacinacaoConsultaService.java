package com.pecucore.system.core.service.vacinacao;

import com.pecucore.system.core.model.Vacinacao;
import com.pecucore.system.core.repository.VacinacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class VacinacaoConsultaService {

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    public boolean existsVacinacaoByVacinaId(Long vacinaId) {
        return vacinacaoRepository.existsByVacinaId(vacinaId);
    }

    public Optional<LocalDate> getDataFimCarenciaVigente(Long animalId, LocalDate dataReferencia) {
        return vacinacaoRepository
                .findFirstByAnimalIdAndDataFimCarenciaAfterOrderByDataFimCarenciaDesc(animalId, dataReferencia)
                .map(Vacinacao::getDataFimCarencia);
    }
}
