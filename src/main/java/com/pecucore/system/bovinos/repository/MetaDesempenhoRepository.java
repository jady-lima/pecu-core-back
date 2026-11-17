package com.pecucore.system.bovinos.repository;

import com.pecucore.system.bovinos.model.FinalidadeLote;
import com.pecucore.system.bovinos.model.MetaDesempenho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetaDesempenhoRepository extends JpaRepository<MetaDesempenho, Long> {

    Optional<MetaDesempenho> findByFase(FinalidadeLote fase);
}