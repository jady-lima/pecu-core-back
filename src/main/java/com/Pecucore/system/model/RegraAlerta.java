package com.Pecucore.system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "regra_alerta")
public class RegraAlerta{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipo;

    @Column(nullable = false)
    private double parametro;

    @Column(nullable = false)
    private boolean ativa;

    @ManyToOne
    @JoinColumn(name = "propriedade_id", nullable = false)
    private Propriedade propriedade;

    public Long getId(){
        return id;
    }

    public TipoAlerta getTipo(){
        return tipo;
    }

    public double getParametro() {
        return parametro;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public Propriedade getPropriedade() {
        return propriedade;
    }

    public void setTipo(TipoAlerta tipo) {
        this.tipo = tipo;
    }

    public void setParametro(double parametro) {
        this.parametro = parametro;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public void setPropriedade(Propriedade propriedade) {
        this.propriedade = propriedade;
    }
}









