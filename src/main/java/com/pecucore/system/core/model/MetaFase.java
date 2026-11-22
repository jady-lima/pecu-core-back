package com.pecucore.system.core.model;

import jakarta.persistence.*;

/**
 * Meta de desempenho cadastrada para uma fase do lote, como faixa (mínimo obrigatório, máximo opcional).
 * Usa a tabela e a coluna que os bovinos já usavam, para preservar as metas cadastradas.
 */
@Entity
@Table(name = "meta_desempenho")
public class MetaFase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Código da fase (FaseLote.codigo) da aplicação ativa. */
    @Column(nullable = false)
    private String fase;

    @Column(name = "gmd_esperado", nullable = false)
    private Double minimo;

    @Column(name = "maximo")
    private Double maximo;

    public Long getId() {
        return id;
    }

    public String getFase() {
        return fase;
    }

    public Double getMinimo() {
        return minimo;
    }

    public Double getMaximo() {
        return maximo;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }

    public void setMinimo(Double minimo) {
        this.minimo = minimo;
    }

    public void setMaximo(Double maximo) {
        this.maximo = maximo;
    }
}