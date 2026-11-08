package com.pecucore.system.model;

import jakarta.persistence.*;

@Entity
@Table(name="pesagem")
public class Pesagem extends RegistroAnimal {

    private double pesoKg;
    private Double gmdCalculado;

    public double getPesoKg() {
        return pesoKg;
    }

    public Double getGmdCalculado() {
        return gmdCalculado;
    }

    public void setPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public void setGmdCalculado(Double gmdCalculado) {
        this.gmdCalculado = gmdCalculado;
    }
}
