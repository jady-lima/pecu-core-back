package com.Pecucore.system.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "lote")
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int numero;
    @Enumerated(EnumType.STRING)
    private FinalidadeLote finalidade;
    private int capacidade;
    private LocalDate dataCriacao;
    private Double gmdMeta;

    @ManyToOne
    @JoinColumn(name = "propriedade_id")
    private Propriedade propriedade;

    public int getNumero(){return numero;}
    public FinalidadeLote getFinalidade(){return finalidade;}
    public LocalDate getDataCriacao(){return dataCriacao;}
    public int getCapacidade(){return capacidade;}
    public Long getId(){return id;}
    public Propriedade getPropriedade() {return propriedade;}
    public Double getGmdMeta(){return gmdMeta;}


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

    public void setGmdMeta(Double gmdMeta) {
        this.gmdMeta = gmdMeta;
    }
}
