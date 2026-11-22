package com.pecucore.system.core.repository;

import com.pecucore.system.core.model.MetaFase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetaFaseRepository extends JpaRepository<MetaFase, Long> {

    Optional<MetaFase> findByFase(String fase);
}