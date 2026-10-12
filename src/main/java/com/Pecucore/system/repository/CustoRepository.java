package com.Pecucore.system.repository;

import com.Pecucore.system.model.Custo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustoRepository extends JpaRepository<Custo, Long> {
    List<Custo> findByAnimalIdOrderByDataAscIdAsc(Long animalId);
}