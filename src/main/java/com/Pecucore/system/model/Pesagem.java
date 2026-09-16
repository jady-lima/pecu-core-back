package com.Pecucore.system.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name="pesagem")
public class Pesagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataPesagem;
    private double peso;

    @ManyToOne
    @JoinColumn(name = "animal_id")
    private Animal animal;




    public Long getId() {return id;}

    public LocalDate getDataPesagem() {return dataPesagem;}

    public double getPeso() {return peso;}

    public Animal getAnimal() {return animal;}

    public void setDataPesagem(LocalDate dataPesagem) {
        this.dataPesagem = dataPesagem;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }
}
