package com.Pecucore.system.repository;

import com.Pecucore.system.model.Vacinacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VacinacaoRepository extends JpaRepository<Vacinacao, Long> {

    List<Vacinacao> findByAnimalIdOrderByDataAscIdAsc(Long animalId);

    boolean existsByAnimalIdAndDataFimCarenciaAfter(Long animalId, LocalDate data);

    boolean existsByVacinaId(Long vacinaId);
}
