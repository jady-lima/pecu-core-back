package com.pecucore.system.core.repository;

import com.pecucore.system.core.model.Alerta;
import com.pecucore.system.core.model.StatusAlerta;
import com.pecucore.system.core.model.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    boolean existsByAnimalIdAndTipoAndStatusIn(Long animalId, TipoAlerta tipo, List<StatusAlerta> status);
    List<Alerta> findByAnimalIdOrderByDataCriacaoDesc(Long animalId);
}