package com.pecucore.system.core.service.medicamento;

import com.pecucore.system.core.model.Medicamento;
import com.pecucore.system.core.repository.MedicamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class MedicamentoConsultaService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    public Optional<LocalDate> getDataFimCarenciaVigente(Long animalId, LocalDate dataReferencia) {
        return medicamentoRepository
                .findFirstByAnimalIdAndDataFimCarenciaAfterOrderByDataFimCarenciaDesc(animalId, dataReferencia)
                .map(Medicamento::getDataFimCarencia);
    }
}
