package com.pecucore.system.core.repository;

import com.pecucore.system.core.model.RegraAlerta;
import com.pecucore.system.core.model.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegraAlertaRepository extends JpaRepository<RegraAlerta, Long> {
    List<RegraAlerta> findByPropriedadeIdAndTipoAndAtivaTrue(Long propriedadeId, TipoAlerta tipo);
}