package com.pecucore.system.bovinos.agrupamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetaDesempenhoRepository extends JpaRepository<MetaDesempenho, Long> {

    Optional<MetaDesempenho> findByFase(FinalidadeLote fase);
}