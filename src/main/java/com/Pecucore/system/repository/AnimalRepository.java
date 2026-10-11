package com.Pecucore.system.repository;

import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.StatusAnimal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    boolean existsByBrinco(int brinco);

    boolean existsByBrincoAndIdNot(int brinco, Long id);

    Optional<Animal> findByBrinco(int brinco);

    List<Animal> findByLoteIdAndStatus(
            Long loteId,
            StatusAnimal status
    );

    boolean existsByLoteIdAndStatus(
            Long loteId,
            StatusAnimal status
    );

    List<Animal> findByLotePropriedadeUsuarioId(
            Long usuarioId
    );
}