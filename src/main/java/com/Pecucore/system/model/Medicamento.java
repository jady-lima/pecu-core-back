package com.Pecucore.system.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "medicamento")
public class Medicamento extends RegistroAnimal{

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false)
    private double dosagem;

    @Column(nullable = false)
    private int carenciaDias;

    @Column(nullable = false)
    private LocalDate dataFimCarencia;

    private Double valor;

    @OneToOne
    @JoinColumn(name = "custo_id")
    private Custo custo;

    public String getNome() {
        return nome;
    }

    public double getDosagem() {
        return dosagem;
    }

    public String getMotivo() {
        return motivo;
    }

    public int getCarenciaDias() {
        return carenciaDias;
    }

    public LocalDate getDataFimCarencia() {
        return dataFimCarencia;
    }

    public Double getValor() {
        return valor;
    }


    public void setCarenciaDias(int carenciaDias) {
        this.carenciaDias = carenciaDias;
    }

    public void setDataFimCarencia(LocalDate dataFimCarencia) {
        this.dataFimCarencia = dataFimCarencia;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public void setDosagem(Double dosagem) {
        this.dosagem = dosagem;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Custo getCusto() {
        return custo;
    }

    public void setCusto(Custo custo) {
        this.custo = custo;
    }
}
