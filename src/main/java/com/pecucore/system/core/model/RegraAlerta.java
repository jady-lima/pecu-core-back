package com.pecucore.system.core.model;

import com.pecucore.system.core.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "regra_alerta")
public class RegraAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipo;

    @Column(nullable = false)
    private double parametro;

    @Column(nullable = false)
    private boolean ativa;

    @ManyToOne
    @JoinColumn(name = "propriedade_id", nullable = false)
    private Propriedade propriedade;

    protected RegraAlerta() {
    }

    public static RegraAlerta criar(Propriedade propriedade, TipoAlerta tipo, double parametro) {
        Objects.requireNonNull(propriedade, "A propriedade da regra é obrigatória.");
        validarParametro(tipo, parametro);

        RegraAlerta regra = new RegraAlerta();
        regra.propriedade = propriedade;
        regra.tipo = tipo;
        regra.parametro = parametro;
        regra.ativa = true;
        return regra;
    }

    public void atualizar(double parametro, boolean ativa) {
        validarParametro(tipo, parametro);

        this.parametro = parametro;
        this.ativa = ativa;
    }

    private static void validarParametro(TipoAlerta tipo, double parametro) {
        if (tipo == null) {
            throw new RegraNegocioException("O tipo de alerta deve ser informado.");
        }

        if (!tipo.isConfiguravel()) {
            throw new RegraNegocioException("Essa regra é fixa do sistema e não pode ser configurada.");
        }

        if (!tipo.getUnidadeParametro().aceita(parametro)) {
            throw new RegraNegocioException("O parâmetro deve ser " + tipo.getUnidadeParametro().getRegraValor() + ".");
        }
    }

    public Long getId() {
        return id;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public double getParametro() {
        return parametro;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public Propriedade getPropriedade() {
        return propriedade;
    }
}
