package com.Pecucore.system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "propriedade")

public class Propriedade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nome;
    private String localizacao;

    public String getNome(){return nome;}
    public String getLocalizacao(){return localizacao;}
    public long getId(){return id;}

    public void setNome(String nome){this.localizacao = nome;}
    public void setLocalizacao(String localizacao){this.localizacao = nome;}

}
