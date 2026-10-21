package com.Pecucore.system.service;

import com.Pecucore.system.model.*;
import com.Pecucore.system.repository.AlertaRepository;
import com.Pecucore.system.repository.RegraAlertaRepository;
import com.Pecucore.system.repository.VacinacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertaSanitarioService {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private RegraAlertaRepository regraAlertaRepository;

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    @Autowired
    private AlertaService alertaService;


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

            alertaService.criarAlerta(
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

            alertaService.criarAlerta(
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

            alertaService.criarAlerta(
                    animal,
                    TipoAlerta.PERIODO_CARENCIA,
                    "Animal ainda está em periodo de carencia.Animal estará em período de carência até "
                            + vacinacao.getDataFimCarencia()
            );

            return;
        }


    }
}