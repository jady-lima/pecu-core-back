package com.Pecucore.system.repository;

import com.Pecucore.system.model.Custo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustoRepository extends JpaRepository<Custo, Long> {
    List<Custo> findByAnimalIdOrderByDataAscIdAsc(Long animalId);

    @Query("""
            SELECT c FROM Custo c
            LEFT JOIN c.animal a
            LEFT JOIN a.lote al
            LEFT JOIN al.propriedade ap
            LEFT JOIN c.lote l
            LEFT JOIN l.propriedade lp
            LEFT JOIN c.propriedade p
            WHERE ap.usuario.id = :usuarioId
               OR lp.usuario.id = :usuarioId
               OR p.usuario.id = :usuarioId
            ORDER BY c.data DESC, c.id DESC
        """)
    List<Custo> findByProdutor(@Param("usuarioId") Long usuarioId);
}
