package com.Pecucore.system.repository;

import com.Pecucore.system.model.Alimentacao;
import com.Pecucore.system.model.RegraAlerta;
import com.Pecucore.system.model.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegraAlertaRepository extends JpaRepository<RegraAlerta, Long> {
    List<RegraAlerta> findByPropriedadeIdAndAtivaTrue(Long propriedadeId);
    List<RegraAlerta> findByPropriedadeIdAndTipoAndAtivaTrue(Long propriedadeId, TipoAlerta tipo);
}