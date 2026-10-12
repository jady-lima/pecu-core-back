package com.Pecucore.system.repository;

import com.Pecucore.system.model.FinalidadeLote;
import com.Pecucore.system.model.MetaDesempenho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetaDesempenhoRepository extends JpaRepository<MetaDesempenho, Long> {

    Optional<MetaDesempenho> findByFase(FinalidadeLote fase);
}