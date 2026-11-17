package com.pecucore.system.core.repository;

import com.pecucore.system.core.model.Alerta;
import com.pecucore.system.core.model.StatusAlerta;
import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    boolean existsByAnimalIdAndTipoAndStatusIn(Long animalId, TipoAlerta tipo, List<StatusAlerta> status);
    List<Alerta> findByAnimalIdOrderByDataCriacaoDesc(Long animalId);
    List<Alerta> findByAnimalIdAndTipoAndStatusIn(Long animalId, TipoAlerta tipo, List<StatusAlerta> status);
    @Query("""
            select count(a) > 0 from Alerta a
            where a.animal.id = :animalId
              and a.tipo = :tipo
              and a.referencia = :referencia
              and a.status in :encerrados
              and a.resolvidoAutomaticamente = false
            """)
    boolean existsFechadoPeloUsuario(@Param("animalId") Long animalId,
                                     @Param("tipo") TipoAlerta tipo,
                                     @Param("referencia") String referencia,
                                     @Param("encerrados") List<StatusAlerta> encerrados);
    List<Alerta> findAllByOrderByDataCriacaoDesc();
    List<Alerta> findByAnimalLotePropriedadeUsuarioIdOrderByDataCriacaoDesc(Long usuarioId);
    List<Alerta> findByAnimalLotePropriedadeVeterinariosIdOrderByDataCriacaoDesc(Long veterinarioId);
}
