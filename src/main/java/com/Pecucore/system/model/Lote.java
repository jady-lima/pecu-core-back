package com.Pecucore.system.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "lote")
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int numero;
    @Enumerated(EnumType.STRING)
    private FinalidadeLote finalidade;
    private int capacidade;
    private LocalDate dataCriacao;

    @ManyToOne
    @JoinColumn(name = "propriedade_id")
    private Propriedade propriedade;

    public int getNumero(){return numero;}
    public FinalidadeLote getFinalidade(){return finalidade;}
    public LocalDate getDataCriacao(){return dataCriacao;}
    public int getCapacidade(){return capacidade;}
    public long getId(){return id;}
    public Propriedade getPropriedade() {return propriedade;}


    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public void setFinalidade(FinalidadeLote finalidade) {
        this.finalidade = finalidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    public void setPropriedade(Propriedade propriedade) {
        this.propriedade = propriedade;
    }



}
