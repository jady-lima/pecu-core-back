package com.Pecucore.system.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerta")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeveridadeAlerta severidade;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAlerta status;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    public Long getId() {
        return id;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public SeveridadeAlerta getSeveridade() {
        return severidade;
    }

    public String getMensagem() {
        return mensagem;
    }

    public StatusAlerta getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setTipo(TipoAlerta tipo) {
        this.tipo = tipo;
    }

    public void setSeveridade(SeveridadeAlerta severidade) {
        this.severidade = severidade;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public void setStatus(StatusAlerta status) {
        this.status = status;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }
}