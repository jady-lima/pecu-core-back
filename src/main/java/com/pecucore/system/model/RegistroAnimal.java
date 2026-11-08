package com.pecucore.system.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "registro_animal")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class RegistroAnimal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "animal_id")
    private Animal animal;

    public Long getId() {
        return id;
    }

    public LocalDate getData() {
        return data;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }
}
