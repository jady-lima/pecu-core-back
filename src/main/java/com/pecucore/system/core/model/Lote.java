package com.pecucore.system.core.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "lote")
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int numero;
    private String finalidade;
    private int capacidade;
    private LocalDate dataCriacao;

    @Column(name = "gmd_meta")
    private Double metaDesempenho;

    @ManyToOne
    @JoinColumn(name = "propriedade_id")
    private Propriedade propriedade;

    public int getNumero(){return numero;}
    public String getFinalidade(){return finalidade;}
    public LocalDate getDataCriacao(){return dataCriacao;}
    public int getCapacidade(){return capacidade;}
    public Long getId(){return id;}
    public Propriedade getPropriedade() {return propriedade;}
    public Double getMetaDesempenho(){return metaDesempenho;}


    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public void setFinalidade(String finalidade) {
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

    public void setMetaDesempenho(Double metaDesempenho) {
        this.metaDesempenho = metaDesempenho;
    }
}
