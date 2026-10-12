package com.Pecucore.system.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analise_ia")
public class AnaliseIA {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAnalise tipo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_solicitante_id", nullable = false)
    private Usuario solicitante;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataGeracao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(nullable = false, length = 100)
    private String modeloUtilizado;

    @Column(nullable = false)
    private boolean incluiDadosFinanceiros;

    @PrePersist
    protected void onCreate() {
        this.dataGeracao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public TipoAnalise getTipo() {
        return tipo;
    }

    public void setTipo(TipoAnalise tipo) {
        this.tipo = tipo;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getModeloUtilizado() {
        return modeloUtilizado;
    }

    public void setModeloUtilizado(String modeloUtilizado) {
        this.modeloUtilizado = modeloUtilizado;
    }

    public boolean isIncluiDadosFinanceiros() {
        return incluiDadosFinanceiros;
    }

    public void setIncluiDadosFinanceiros(boolean incluiDadosFinanceiros) {
        this.incluiDadosFinanceiros = incluiDadosFinanceiros;
    }
}
