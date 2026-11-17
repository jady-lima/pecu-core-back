package com.pecucore.system.bovinos.service.sanidade;

import com.pecucore.system.core.model.Medicamento;
import com.pecucore.system.core.model.Vacinacao;
import com.pecucore.system.core.repository.MedicamentoRepository;
import com.pecucore.system.core.repository.VacinacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class CarenciaService {

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    public boolean estaEmCarencia(Long animalId) {
        return getDataFimCarencia(animalId).isPresent();
    }

    public Optional<LocalDate> getDataFimCarencia(Long animalId) {
        LocalDate hoje = LocalDate.now();

        return Stream.of(
                    vacinacaoRepository.findFirstByAnimalIdAndDataFimCarenciaAfterOrderByDataFimCarenciaDesc(animalId, hoje).map(Vacinacao::getDataFimCarencia),
                    medicamentoRepository.findFirstByAnimalIdAndDataFimCarenciaAfterOrderByDataFimCarenciaDesc(animalId, hoje).map(Medicamento::getDataFimCarencia)
                )
                .flatMap(Optional::stream)
                .max(LocalDate::compareTo);
    }
}
