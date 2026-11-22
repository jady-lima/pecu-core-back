package com.pecucore.system.core.repository;

import com.pecucore.system.core.model.Vacina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacinaRepository extends JpaRepository<Vacina, Long> {

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);

    List<Vacina> findByExigidaParaTransitoTrue();
}