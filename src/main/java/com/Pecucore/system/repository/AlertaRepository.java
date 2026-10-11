package com.Pecucore.system.repository;

import com.Pecucore.system.model.Alerta;
import com.Pecucore.system.model.Alimentacao;
import com.Pecucore.system.model.StatusAlerta;
import com.Pecucore.system.model.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    boolean existsByAnimalIdAndTipoAndStatusIn(Long animalId, TipoAlerta tipo, List<StatusAlerta> status);
}