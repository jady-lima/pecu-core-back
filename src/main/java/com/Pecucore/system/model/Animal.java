package com.pecucore.system.model;

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

    //ANIMAL

    private int brinco;
    private LocalDate dataNascimento;
    private double pesoInicial;
    private int lote;
    private String sexo;


    public int getLote() {
        return lote;
    }

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

    public void setBrinco(int brinco) {
        this.brinco = brinco;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public void setPesoInicial(double pesoInicial) {
        this.pesoInicial = pesoInicial;
    }

    public void setLote(int lote) {
        this.lote = lote;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }
}



