package com.Pecucore.system.repository;

import com.Pecucore.system.model.Propriedade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropriedadeRepository extends JpaRepository<Propriedade, Long> {
    List<Propriedade> findByUsuarioId(Long usuarioId);
    List<Propriedade> findByVeterinariosId(Long veterinarioId);
    boolean existsByIdAndVeterinariosId(Long propriedadeId, Long veterinarioId);
}
