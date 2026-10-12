package com.Pecucore.system.service;

import com.Pecucore.system.model.*;
import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.repository.AlertaRepository;
import com.Pecucore.system.repository.PesagemRepository;
import com.Pecucore.system.repository.RegraAlertaRepository;
import com.Pecucore.system.model.Vacinacao;
import com.Pecucore.system.repository.VacinacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public void verificarGmdAbaixoMeta(Animal animal, Double gmd) {
        if (gmd == null) {
            return;
        }

        Propriedade propriedade = animal.getLote().getPropriedade();

        RegraAlerta regra = regraAlertaRepository.findByPropriedadeIdAndTipoAndAtivaTrue(propriedade.getId(), TipoAlerta.GMD_ABAIXO_META)
                .stream()
                .findFirst()
                .orElse(null);

        if (regra == null) {
            return;
        }
        if (gmd >= regra.getParametro()) {
            return;
        }

        boolean alertaJaExiste = alertaRepository
                .existsByAnimalIdAndTipoAndStatusIn(animal.getId(), TipoAlerta.GMD_ABAIXO_META, java.util.List.of(
                                StatusAlerta.ABERTO,
                                StatusAlerta.EM_ANALISE
                        )
                );
        if (alertaJaExiste) {
            return;
        }

        criarAlerta(
                animal,
                TipoAlerta.GMD_ABAIXO_META,
                "GMD do animal abaixo da meta estabelecida: " + gmd + "kg/dia"
        );

    }

    public void verificarQuedaPeso(Animal animal) {

        List<Pesagem> pesagens = pesagemRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());

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
                .existsByAnimalIdAndTipoAndStatusIn(animal.getId(), TipoAlerta.QUEDA_PESO, List.of(
                                StatusAlerta.ABERTO,
                                StatusAlerta.EM_ANALISE
                        )
                );

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

        RegraAlerta regra = regraAlertaRepository.findByPropriedadeIdAndTipoAndAtivaTrue(animal.getLote().getPropriedade().getId(), TipoAlerta.PESAGEM_ATRASADA
                )
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

        boolean alertaJaExiste = alertaRepository.existsByAnimalIdAndTipoAndStatusIn(animal.getId(), TipoAlerta.PESAGEM_ATRASADA,
                List.of(
                        StatusAlerta.ABERTO,
                        StatusAlerta.EM_ANALISE
                )
        );

        if (alertaJaExiste) {
            return;
        }

        criarAlerta(
                animal,
                TipoAlerta.PESAGEM_ATRASADA,
                "PESAGEM DO ANIMAL ESTÁ ATRASADA: Animal está há " + diasSemPesagem
                        + " dias sem nova pesagem."
        );
    }

    public void verificarVacinaProximaVencimento(Animal animal) {


        List<Vacinacao> vacinacoes = vacinacaoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (vacinacoes.isEmpty()) {
            return;
        }

        RegraAlerta regra = regraAlertaRepository.findByPropriedadeIdAndTipoAndAtivaTrue(animal.getLote().getPropriedade().getId(), TipoAlerta.VACINA_PROX_VENC
                )
                .stream()
                .findFirst()
                .orElse(null);

        if (regra == null) {
            return;
        }

        LocalDate hoje = LocalDate.now();

        for (Vacinacao vacinacao : vacinacoes) {

            if (vacinacao.getDataProximaDose() == null) {
                continue;
            }

            long diasParaProximaDose = ChronoUnit.DAYS.between(
                    hoje,
                    vacinacao.getDataProximaDose()
            );

            if (diasParaProximaDose < 0 ||
                    diasParaProximaDose > regra.getParametro()) {
                continue;
            }

            boolean alertaJaExiste = alertaRepository
                    .existsByAnimalIdAndTipoAndStatusIn(animal.getId(), TipoAlerta.VACINA_PROX_VENC,
                            List.of(
                                    StatusAlerta.ABERTO,
                                    StatusAlerta.EM_ANALISE
                            )
                    );

            if (alertaJaExiste) {
                return;
            }

            criarAlerta(
                    animal,
                    TipoAlerta.VACINA_PROX_VENC,
                    "Atenção: Vacinação proxima. A próxima dose da vacina está prevista para "
                            + vacinacao.getDataProximaDose()
            );

            return;
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

            if (vacinacao.getDataProximaDose() == null) {
                continue;
            }

            if (!vacinacao.getDataProximaDose().isBefore(hoje)) {
                continue;
            }

            boolean alertaJaExiste = alertaRepository.existsByAnimalIdAndTipoAndStatusIn(animal.getId(), TipoAlerta.VACINA_VENC,
                    List.of(
                            StatusAlerta.ABERTO,
                            StatusAlerta.EM_ANALISE
                    )
            );

            if (alertaJaExiste) {
                return;
            }

            criarAlerta(
                    animal,
                    TipoAlerta.VACINA_VENC,
                    "A vacina do animal está vencida. A próxima dose estava prevista para "
                            + vacinacao.getDataProximaDose()
            );

            return;
        }
    }

        public void verificarPeriodoCarencia (Animal animal){


            List<Vacinacao> vacinacoes = vacinacaoRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());

            if (vacinacoes.isEmpty()) {
                return;
            }

            LocalDate hoje = LocalDate.now();

            for (Vacinacao vacinacao : vacinacoes) {

                if (vacinacao.getDataFimCarencia() == null) {
                    continue;
                }

                if (!vacinacao.getDataFimCarencia().isAfter(hoje)) {
                    continue;
                }

                boolean alertaJaExiste = alertaRepository.existsByAnimalIdAndTipoAndStatusIn(animal.getId(), TipoAlerta.PERIODO_CARENCIA,
                        List.of(
                                StatusAlerta.ABERTO,
                                StatusAlerta.EM_ANALISE
                        )
                );

                if (alertaJaExiste) {
                    return;
                }

                criarAlerta(
                        animal,
                        TipoAlerta.PERIODO_CARENCIA,
                        "Animal ainda está em periodo de carencia.Animal estará em período de carência até "
                                + vacinacao.getDataFimCarencia()
                );

                return;
            }


        }



    public List<Alerta> listarAlertas() {
        return alertaRepository.findAll();
    }

    public Alerta atualizarStatus(Long id, StatusAlerta novoStatus) {

        Alerta alerta = alertaRepository.findById(id).orElseThrow(() -> new RuntimeException("Alerta não encontrado"));
        alerta.setStatus(novoStatus);
        return alertaRepository.save(alerta);
    }




    private void criarAlerta(
            Animal animal,
            TipoAlerta tipo,
            String mensagem
    ) {
        Alerta alerta = new Alerta();

        alerta.setAnimal(animal);
        alerta.setTipo(tipo);
        alerta.setSeveridade(SeveridadeAlerta.MEDIA);
        alerta.setMensagem(mensagem);
        alerta.setStatus(StatusAlerta.ABERTO);
        alerta.setDataCriacao(java.time.LocalDateTime.now());

        alertaRepository.save(alerta);

    }
    }



