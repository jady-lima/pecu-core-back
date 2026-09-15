package com.Pecucore.system.repository;

import com.Pecucore.system.model.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import com.Pecucore.system.model.StatusAnimal;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    boolean existsByBrinco(int brinco);

    boolean existsByBrincoAndIdNot(int brinco, Long id);

    boolean existsByLoteIdAndStatus(Long loteId, StatusAnimal status);
}
