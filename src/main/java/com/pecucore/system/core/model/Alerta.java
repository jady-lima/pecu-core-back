package com.pecucore.system.core.model;

import com.pecucore.system.core.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

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

    private LocalDateTime dataResolucao;

    @ManyToOne
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private Lote lote;

    protected Alerta() {
    }

    public static Alerta abrir(Animal animal, TipoAlerta tipo, String mensagem) {
        Objects.requireNonNull(animal, "O animal do alerta é obrigatório.");
        Objects.requireNonNull(tipo, "O tipo do alerta é obrigatório.");

        Alerta alerta = new Alerta();
        alerta.animal = animal;
        alerta.lote = animal.getLote();
        alerta.tipo = tipo;
        alerta.severidade = tipo.getSeveridade();
        alerta.mensagem = mensagem;
        alerta.status = StatusAlerta.ABERTO;
        alerta.dataCriacao = LocalDateTime.now();
        return alerta;
    }

    public void alterarStatus(StatusAlerta novoStatus) {
        if (novoStatus == null) {
            throw new RegraNegocioException("O status é obrigatório.");
        }

        if (!status.podeMudarPara(novoStatus)) {
            throw new RegraNegocioException("Não é possível alterar o status de " + status + " para " + novoStatus + ".");
        }

        this.status = novoStatus;
        this.dataResolucao = novoStatus.isEncerrado() ? LocalDateTime.now() : null;
    }

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

    public LocalDateTime getDataResolucao() {
        return dataResolucao;
    }

    public Animal getAnimal() {
        return animal;
    }

    public Lote getLote() {
        return lote;
    }
}
