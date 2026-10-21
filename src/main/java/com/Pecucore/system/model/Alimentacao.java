package com.Pecucore.system.model;

import jakarta.persistence.*;

@Entity
@Table(name="alimentacao")
public class Alimentacao extends RegistroAnimal {

    @Enumerated(EnumType.STRING)
    private TipoAlimentacao tipo;

    private double quantidade;

    private Double valor;

    @OneToOne
    @JoinColumn(name = "custo_id")
    private Custo custo;

    public TipoAlimentacao getTipo() {
        return tipo;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public void setTipo(TipoAlimentacao tipo) {
        this.tipo = tipo;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public Custo getCusto() {
        return custo;
    }

    public void setCusto(Custo custo) {
        this.custo = custo;
    }
}
