package com.pecucore.system.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "custo")
public class Custo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCusto tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemCusto origem;

    @Column(nullable = false)
    private double valor;

    @Column(nullable = false)
    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @ManyToOne
    @JoinColumn(name = "propriedade_id")
    private Propriedade propriedade;

    public Long getId() {
        return id;
    }

    public TipoCusto getTipo() {
        return tipo;
    }

    public OrigemCusto getOrigem() {
        return origem;
    }

    public double getValor() {
        return valor;
    }

    public LocalDate getData() {
        return data;
    }

    public Animal getAnimal() {
        return animal;
    }

    public Lote getLote() {
        return lote;
    }

    public Propriedade getPropriedade() {
        return propriedade;
    }

    public void setTipo(TipoCusto tipo) {
        this.tipo = tipo;
    }

    public void setOrigem(OrigemCusto origem) {
        this.origem = origem;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public void setPropriedade(Propriedade propriedade) {
        this.propriedade = propriedade;
    }
}