package com.pecucore.system.core.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="animal")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int brinco;
    @Column(updatable = false)
    private LocalDate dataNascimento;
    private double pesoAtual;
    private String sexo;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @Enumerated(EnumType.STRING)
    private StatusAnimal status;

    @Enumerated(EnumType.STRING)
    private SituacaoGeralAnimal situacaoGeral;

    private LocalDateTime dataUltimaAvaliacao;

    public Long getId(){return id;}

    public int getBrinco() {
        return brinco;
    }

    public String getSexo() {
        return sexo;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public double getPesoAtual() {
        return pesoAtual;
    }

    public StatusAnimal getStatus(){return status;}

    public Lote getLote(){return lote;}

    public void setBrinco(int brinco) {
        this.brinco = brinco;
    }

    public void setStatus(StatusAnimal status){this.status = status;}

    public void setLote(Lote lote){this.lote = lote;}

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public void setPesoAtual(double pesoInicial) {
        this.pesoAtual = pesoInicial;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public SituacaoGeralAnimal getSituacaoGeral() {
        return situacaoGeral;
    }

    public void setSituacaoGeral(SituacaoGeralAnimal situacaoGeral) {
        this.situacaoGeral = situacaoGeral;
    }

    public LocalDateTime getDataUltimaAvaliacao() {
        return dataUltimaAvaliacao;
    }

    public void setDataUltimaAvaliacao(LocalDateTime dataUltimaAvaliacao) {
        this.dataUltimaAvaliacao = dataUltimaAvaliacao;
    }
}



