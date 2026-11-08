package com.pecucore.system.repository;

import com.pecucore.system.model.AnaliseIA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnaliseIARepository extends JpaRepository<AnaliseIA, Long> {
    List<AnaliseIA> findByAnimalIdOrderByDataGeracaoDesc(Long animalId);
}
