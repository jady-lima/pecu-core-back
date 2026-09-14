package com.Pecucore.system.model;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name="animal")
public class Animal implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int brinco;
    private LocalDate dataNascimento;
    private double pesoInicial;
    private String sexo;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @Enumerated(EnumType.STRING)
    private StatusAnimal status;



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

    public double getPesoInicial() {
        return pesoInicial;
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

    public void setPesoInicial(double pesoInicial) {
        this.pesoInicial = pesoInicial;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }
}



