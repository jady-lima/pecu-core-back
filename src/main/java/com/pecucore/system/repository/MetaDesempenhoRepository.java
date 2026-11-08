package com.pecucore.system.repository;

import com.pecucore.system.model.FinalidadeLote;
import com.pecucore.system.model.MetaDesempenho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetaDesempenhoRepository extends JpaRepository<MetaDesempenho, Long> {

    Optional<MetaDesempenho> findByFase(FinalidadeLote fase);
}