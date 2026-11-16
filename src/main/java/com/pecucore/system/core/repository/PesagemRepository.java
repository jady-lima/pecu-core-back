package com.pecucore.system.core.repository;

import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.StatusAnimal;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PesagemRepository extends JpaRepository<Pesagem, Long> {
    Optional<Pesagem> findTopByAnimalIdOrderByDataDescIdDesc(Long animalId);
    List<Pesagem> findByAnimalIdOrderByDataAscIdAsc(Long animalId);
    List<Pesagem> findByAnimalIdOrderByDataDescIdDesc(Long animalId, Limit limit);

    @EntityGraph(attributePaths = "animal")
    List<Pesagem> findByAnimalLoteIdAndAnimalStatusOrderByAnimalIdAscDataAscIdAsc(Long loteId, StatusAnimal status);
}
