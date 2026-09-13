package com.Pecucore.system.repository;

import com.Pecucore.system.model.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    boolean existsByBrinco(int brinco);

    boolean existsByBrincoAndIdNot(int brinco, Long id);
}
