package com.pecucore.system.core.repository;

import com.pecucore.system.core.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    List<Medicamento> findByAnimalIdOrderByDataAscIdAsc(Long animalId);

    Optional<Medicamento> findFirstByAnimalIdAndDataFimCarenciaAfterOrderByDataFimCarenciaDesc(Long animalId, LocalDate data);
}