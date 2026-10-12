package com.Pecucore.system.repository;

import com.Pecucore.system.model.Propriedade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropriedadeRepository extends JpaRepository<Propriedade, Long> {
    boolean existsByIdAndVeterinariosId(Long propriedadeId, Long veterinarioId);
    List<Propriedade> findByUsuarioId(Long usuarioId);
}
