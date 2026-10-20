package com.Pecucore.system.service;

import com.Pecucore.system.model.*;
import com.Pecucore.system.repository.AlertaRepository;
import com.Pecucore.system.repository.PesagemRepository;
import com.Pecucore.system.repository.RegraAlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertaPesoService {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private RegraAlertaRepository regraAlertaRepository;

    @Autowired
    private PesagemRepository pesagemRepository;

    @Autowired
    private AlertaService alertaService;

    public void verificarGmdAbaixoMeta(Animal animal, Double gmd) {
        if (gmd == null) {
            return;
        }

        Propriedade propriedade = animal.getLote().getPropriedade();

        RegraAlerta regra = regraAlertaRepository
                .findByPropriedadeIdAndTipoAndAtivaTrue(
                        propriedade.getId(),
                        TipoAlerta.GMD_ABAIXO_META
                )
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
                .existsByAnimalIdAndTipoAndStatusIn(
                        animal.getId(),
                        TipoAlerta.GMD_ABAIXO_META,
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
                TipoAlerta.GMD_ABAIXO_META,
                "GMD do animal abaixo da meta estabelecida: " + gmd + "kg/dia"
        );
    }

    public void verificarQuedaPeso(Animal animal) {
        List<Pesagem> pesagens =
                pesagemRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());

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
                TipoAlerta.QUEDA_PESO,
                "Animal apresentou queda de peso em duas pesagens consecutivas."
        );
    }

    public void verificarPesagemAtrasada(Animal animal) {
        List<Pesagem> pesagens =
                pesagemRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (pesagens.isEmpty()) {
            return;
        }

        Pesagem ultimaPesagem = pesagens.get(pesagens.size() - 1);

        RegraAlerta regra = regraAlertaRepository
                .findByPropriedadeIdAndTipoAndAtivaTrue(
                        animal.getLote().getPropriedade().getId(),
                        TipoAlerta.PESAGEM_ATRASADA
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

        boolean alertaJaExiste = alertaRepository
                .existsByAnimalIdAndTipoAndStatusIn(
                        animal.getId(),
                        TipoAlerta.PESAGEM_ATRASADA,
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
                TipoAlerta.PESAGEM_ATRASADA,
                "PESAGEM DO ANIMAL ESTÁ ATRASADA: Animal está há "
                        + diasSemPesagem
                        + " dias sem nova pesagem."
        );
    }
}