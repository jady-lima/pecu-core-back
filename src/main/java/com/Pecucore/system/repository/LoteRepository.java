
package com.Pecucore.system.repository;

import com.Pecucore.system.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByPropriedadeUsuarioId(Long usuarioId);

    boolean existsByNumeroAndPropriedade_Id(int numero, Long propriedadeId);

    boolean existsByNumeroAndPropriedade_IdAndIdNot(int numero, Long propriedadeId, Long id);
}
