package com.Pecucore.system.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="vacinacao")
public class Vacinacao extends RegistroAnimal {

    @ManyToOne(optional = false)
    @JoinColumn(name = "vacina_id", nullable = false)
    private Vacina vacina;

    @Column(nullable = false, length = 50)
    private String loteVacina;

    @Column(nullable = false)
    private double doseAplicada;

    private LocalDate dataProximaDose;

    @Column(nullable = false)
    private LocalDate dataFimCarencia;

    @Column(length = 500)
    private String observacoes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_aplicador_id", nullable = false)
    private Usuario usuarioAplicador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_registro_id", nullable = false)
    private Usuario usuarioRegistro;

    private Double valor;

    @OneToOne
    @JoinColumn(name = "custo_id")
    private Custo custo;

    public Vacina getVacina() {
        return vacina;
    }

    public void setVacina(Vacina vacina) {
        this.vacina = vacina;
    }

    public String getLoteVacina() {
        return loteVacina;
    }

    public void setLoteVacina(String loteVacina) {
        this.loteVacina = loteVacina;
    }

    public double getDoseAplicada() {
        return doseAplicada;
    }

    public void setDoseAplicada(double doseAplicada) {
        this.doseAplicada = doseAplicada;
    }

    public LocalDate getDataProximaDose() {
        return dataProximaDose;
    }

    public void setDataProximaDose(LocalDate dataProximaDose) {
        this.dataProximaDose = dataProximaDose;
    }

    public LocalDate getDataFimCarencia() {
        return dataFimCarencia;
    }

    public void setDataFimCarencia(LocalDate dataFimCarencia) {
        this.dataFimCarencia = dataFimCarencia;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Usuario getUsuarioAplicador() {
        return usuarioAplicador;
    }

    public void setUsuarioAplicador(Usuario usuarioAplicador) {
        this.usuarioAplicador = usuarioAplicador;
    }

    public Usuario getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(Usuario usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public Double getValor() {
        return valor;
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
