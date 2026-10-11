package com.Pecucore.system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "meta_desempenho")
public class MetaDesempenho {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinalidadeLote fase;

    @Column(nullable = false)
    private double gmdEsperado;

    public Long getId() {
        return id;
    }

    public FinalidadeLote getFase() {
        return fase;
    }

    public double getGmdEsperado() {
        return gmdEsperado;
    }

    public void setFase(FinalidadeLote fase) {
        this.fase = fase;
    }

    public void setGmdEsperado(double gmdEsperado) {
        this.gmdEsperado = gmdEsperado;
    }
}