package com.pecucore.system.repository;

import com.pecucore.system.model.RegraAlerta;
import com.pecucore.system.model.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegraAlertaRepository extends JpaRepository<RegraAlerta, Long> {
    List<RegraAlerta> findByPropriedadeIdAndTipoAndAtivaTrue(Long propriedadeId, TipoAlerta tipo);
}