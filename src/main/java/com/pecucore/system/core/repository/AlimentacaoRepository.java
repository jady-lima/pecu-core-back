package com.pecucore.system.core.repository;

import com.pecucore.system.core.model.Alimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlimentacaoRepository extends JpaRepository<Alimentacao, Long> {
    List<Alimentacao> findByAnimalIdOrderByDataAscIdAsc(Long animalId);
}
