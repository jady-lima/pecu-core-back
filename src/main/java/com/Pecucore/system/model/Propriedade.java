package com.Pecucore.system.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "propriedade")

public class Propriedade {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nome;
    private String localizacao;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToMany
    @JoinTable(
        name = "propriedade_veterinario",
        joinColumns = @JoinColumn(name = "propriedade_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> veterinarios = new HashSet<>();

    public String getNome(){return nome;}
    public String getLocalizacao(){return localizacao;}
    public Long getId(){return id;}
    public Usuario getUsuario(){return usuario;}
    public Set<Usuario> getVeterinarios(){return veterinarios;}

    public void setNome(String nome){this.nome = nome;}
    public void setLocalizacao(String localizacao){this.localizacao = localizacao;}
    public void setUsuario(Usuario usuario){this.usuario = usuario;}
}
