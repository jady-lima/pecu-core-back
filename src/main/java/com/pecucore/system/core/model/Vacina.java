package com.pecucore.system.core.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vacina")
public class Vacina {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String fabricante;

    @Column(nullable = false)
    private int carenciaDias;

    private Integer intervaloDoseDias;


    /**
     * Vacina ou exame (ex.: AIE) que a legislação exige em dia para o animal sair da propriedade (GTA).
     * A validade é o intervaloDoseDias. Cada aplicação decide quais saídas isso bloqueia (PoliticaRestricaoSanitaria).
     */
    @Column(name = "exigida_para_transito", nullable = false, columnDefinition = "boolean default false")
    private boolean exigidaParaTransito;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getFabricante() {
        return fabricante;
    }

    public int getCarenciaDias() {
        return carenciaDias;
    }

    public Integer getIntervaloDoseDias() {
        return intervaloDoseDias;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public void setCarenciaDias(int carenciaDias) {
        this.carenciaDias = carenciaDias;
    }

    public void setIntervaloDoseDias(Integer intervaloDoseDias) {
        this.intervaloDoseDias = intervaloDoseDias;
    }


    public boolean isExigidaParaTransito() {
        return exigidaParaTransito;
    }

    public void setExigidaParaTransito(boolean exigidaParaTransito) {
        this.exigidaParaTransito = exigidaParaTransito;
    }
}
