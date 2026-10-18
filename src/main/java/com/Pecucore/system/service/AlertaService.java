
package com.Pecucore.system.service;

import com.Pecucore.system.model.*;
import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.repository.*;
import com.Pecucore.system.model.Vacinacao;
import com.Pecucore.system.model.Custo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private RegraAlertaRepository regraAlertaRepository;

    @Autowired
    private PesagemRepository pesagemRepository;

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    @Autowired
    private CustoRepository custoRepository;

    public void verificarGmdAbaixoMeta(Animal animal, Double gmd) {
        if (gmd == null) {
            return;
        }

        Propriedade propriedade = animal.getLote().getPropriedade();

        RegraAlerta regra = regraAlertaRepository
                .findByPropriedadeIdAndTipoAndAtivaTrue(
                        propriedade.getId(), TipoAlerta.GMD_ABAIXO_META)
                .stream()
                .findFirst()
                .orElse(null);

        if (regra == null || gmd >= regra.getParametro()) {
            return;
        }

        boolean alertaJaExiste = alertaRepository
                .existsByAnimalIdAndTipoAndStatusIn(
                        animal.getId(),
                        TipoAlerta.GMD_ABAIXO_META,
                        List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE));

        if (alertaJaExiste) {
            return;
        }

        criarAlerta(
                animal,
                TipoAlerta.GMD_ABAIXO_META,
                "GMD do animal abaixo da meta estabelecida: " + gmd + " kg/dia."
        );
    }

    public void verificarQuedaPeso(Animal animal) {
        List<Pesagem> pesagens = pesagemRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (pesagens.size() < 3) {
            return;
        }

        Pesagem ultima = pesagens.get(pesagens.size() - 1);
        Pesagem anterior = pesagens.get(pesagens.size() - 2);
        Pesagem anteriorAnterior = pesagens.get(pesagens.size() - 3);

        boolean primeiraQueda =
                anterior.getPesoKg() < anteriorAnterior.getPesoKg();

        boolean segundaQueda =
                ultima.getPesoKg() < anterior.getPesoKg();

        if (!primeiraQueda || !segundaQueda) {
            return;
        }

        boolean alertaJaExiste = alertaRepository
                .existsByAnimalIdAndTipoAndStatusIn(
                        animal.getId(),
                        TipoAlerta.QUEDA_PESO,
                        List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE));

        if (alertaJaExiste) {
            return;
        }

        criarAlerta(
                animal,
                TipoAlerta.QUEDA_PESO,
                "Animal apresentou queda de peso em duas pesagens consecutivas."
        );
    }

    public void verificarPesagemAtrasada(Animal animal) {
        List<Pesagem> pesagens = pesagemRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (pesagens.isEmpty()) {
            return;
        }

        Pesagem ultimaPesagem = pesagens.get(pesagens.size() - 1);

        RegraAlerta regra = regraAlertaRepository
                .findByPropriedadeIdAndTipoAndAtivaTrue(
                        animal.getLote().getPropriedade().getId(),
                        TipoAlerta.PESAGEM_ATRASADA)
                .stream()
                .findFirst()
                .orElse(null);

        if (regra == null) {
            return;
        }

        long diasSemPesagem = ChronoUnit.DAYS.between(
                ultimaPesagem.getData(),
                LocalDate.now()
        );

        if (diasSemPesagem <= regra.getParametro()) {
            return;
        }

        boolean alertaJaExiste = alertaRepository
                .existsByAnimalIdAndTipoAndStatusIn(
                        animal.getId(),
                        TipoAlerta.PESAGEM_ATRASADA,
                        List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE));

        if (alertaJaExiste) {
            return;
        }

        criarAlerta(
                animal,
                TipoAlerta.PESAGEM_ATRASADA,
                "Pesagem atrasada: animal está há " + diasSemPesagem
                        + " dias sem nova pesagem."
        );
    }

    public void verificarVacinaProximaVencimento(Animal animal) {
        List<Vacinacao> vacinacoes = vacinacaoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (vacinacoes.isEmpty()) {
            return;
        }

        LocalDate hoje = LocalDate.now();

        for (Vacinacao vacinacao : vacinacoes) {
            LocalDate proximaDose = vacinacao.getDataProximaDose();

            if (proximaDose == null) {
                continue;
            }

            long diasParaProximaDose = ChronoUnit.DAYS.between(
                    hoje,
                    proximaDose
            );

            if (diasParaProximaDose != 15
                    && diasParaProximaDose != 7
                    && diasParaProximaDose != 1) {
                continue;
            }

            String identificadorAviso = "Antecedência: "
                    + diasParaProximaDose
                    + " dia(s); dose: "
                    + proximaDose;

            boolean alertaJaExiste = alertaRepository.findAll().stream()
                    .anyMatch(alerta ->
                            alerta.getAnimal() != null
                                    && alerta.getAnimal().getId().equals(animal.getId())
                                    && alerta.getTipo() == TipoAlerta.VACINA_PROX_VENC
                                    && alerta.getMensagem().contains(identificadorAviso)
                    );

            if (alertaJaExiste) {
                continue;
            }

            criarAlerta(
                    animal,
                    TipoAlerta.VACINA_PROX_VENC,
                    "Atenção: vacina com dose prevista para "
                            + proximaDose + ". "
                            + identificadorAviso
            );
        }
    }

    public void verificarVacinaVencida(Animal animal) {
        List<Vacinacao> vacinacoes = vacinacaoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (vacinacoes.isEmpty()) {
            return;
        }

        LocalDate hoje = LocalDate.now();

        for (Vacinacao vacinacao : vacinacoes) {
            LocalDate proximaDose = vacinacao.getDataProximaDose();

            if (proximaDose == null || !proximaDose.isBefore(hoje)) {
                continue;
            }

            boolean alertaJaExiste = alertaRepository
                    .existsByAnimalIdAndTipoAndStatusIn(
                            animal.getId(),
                            TipoAlerta.VACINA_VENC,
                            List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE));

            if (alertaJaExiste) {
                return;
            }

            criarAlerta(
                    animal,
                    TipoAlerta.VACINA_VENC,
                    "Vacina vencida. A próxima dose estava prevista para "
                            + proximaDose + "."
            );

            return;
        }
    }

    public void verificarPeriodoCarencia(Animal animal) {
        List<Vacinacao> vacinacoes = vacinacaoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (vacinacoes.isEmpty()) {
            return;
        }

        LocalDate hoje = LocalDate.now();

        for (Vacinacao vacinacao : vacinacoes) {
            LocalDate fimCarencia = vacinacao.getDataFimCarencia();

            if (fimCarencia == null || !fimCarencia.isAfter(hoje)) {
                continue;
            }

            boolean alertaJaExiste = alertaRepository
                    .existsByAnimalIdAndTipoAndStatusIn(
                            animal.getId(),
                            TipoAlerta.PERIODO_CARENCIA,
                            List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE));

            if (alertaJaExiste) {
                return;
            }

            criarAlerta(
                    animal,
                    TipoAlerta.PERIODO_CARENCIA,
                    "Animal em período de carência até "
                            + fimCarencia
                            + ". Não deve ser destinado à venda ou ao abate."
            );

            return;
        }
    }

    public void verificarCustoAcima(Animal animal) {
        List<Custo> custos = custoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (custos.isEmpty()) {
            return;
        }

        Propriedade propriedade = animal.getLote().getPropriedade();

        RegraAlerta regra = regraAlertaRepository
                .findByPropriedadeIdAndTipoAndAtivaTrue(
                        propriedade.getId(), TipoAlerta.CUSTO_ACIMA)
                .stream()
                .findFirst()
                .orElse(null);

        if (regra == null) {
            return;
        }

        double custoTotal = custos.stream()
                .mapToDouble(Custo::getValor)
                .sum();

        if (custoTotal <= regra.getParametro()) {
            return;
        }

        boolean alertaJaExiste = alertaRepository
                .existsByAnimalIdAndTipoAndStatusIn(
                        animal.getId(),
                        TipoAlerta.CUSTO_ACIMA,
                        List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE));

        if (alertaJaExiste) {
            return;
        }

        criarAlerta(
                animal,
                TipoAlerta.CUSTO_ACIMA,
                "O animal está com custo acumulado de R$ "
                        + custoTotal
                        + ", acima do limite estabelecido de R$ "
                        + regra.getParametro() + "."
        );
    }

    public List<Alerta> listarAlertas() {
        return alertaRepository.findAll();
    }

    public Alerta atualizarStatus(Long id, StatusAlerta novoStatus) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Alerta não encontrado"));

        alerta.setStatus(novoStatus);

        return alertaRepository.save(alerta);
    }

    private SeveridadeAlerta definirSeveridade(TipoAlerta tipo) {
        switch (tipo) {
            case QUEDA_PESO:
            case VACINA_VENC:
            case GMD_ABAIXO_META:
            case CUSTO_ACIMA:
                return SeveridadeAlerta.ALTA;

            case VACINA_PROX_VENC:
                return SeveridadeAlerta.BAIXA;

            case PERIODO_CARENCIA:
            case PESAGEM_ATRASADA:
            default:
                return SeveridadeAlerta.MEDIA;
        }
    }

    private void criarAlerta(
            Animal animal,
            TipoAlerta tipo,
            String mensagem
    ) {
        Alerta alerta = new Alerta();

        alerta.setAnimal(animal);
        alerta.setTipo(tipo);
        alerta.setSeveridade(definirSeveridade(tipo));
        alerta.setMensagem(mensagem);
        alerta.setStatus(StatusAlerta.ABERTO);
        alerta.setDataCriacao(LocalDateTime.now());

        alertaRepository.save(alerta);
    }
}
