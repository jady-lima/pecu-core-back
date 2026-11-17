package com.pecucore.system.core.model;

import jakarta.persistence.*;

@Entity
@Table(name="pesagem")
public class Pesagem extends RegistroAnimal {

    private double pesoKg;

    @Column(name = "gmd_calculado")
    private Double indiceDesempenho;

    public double getPesoKg() {
        return pesoKg;
    }

    public Double getIndiceDesempenho() {
        return indiceDesempenho;
    }

    public void setPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public void setIndiceDesempenho(Double indiceDesempenho) {
        this.indiceDesempenho = indiceDesempenho;
    }
}
