package com.Pecucore.system.repository;

import com.Pecucore.system.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    List<Medicamento> findByAnimalIdOrderByDataAscIdAsc(Long animalId);

    boolean existsByAnimalIdAndDataFimCarenciaAfter(Long animalId, LocalDate data);
}