package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.repository.PesagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class AlertaPesoService {

    @Autowired
    private PesagemRepository pesagemRepository;

    @Autowired
    private AlertaService alertaService;

    @Autowired
    private RegraAlertaService regraAlertaService;

    public void verificarGmdAbaixoMeta(Animal animal, Double gmd) {
        if (gmd == null) {
            return;
        }

        Optional<Double> meta = regraAlertaService.getParametroAtivo(animal, TipoAlerta.GMD_ABAIXO_META);

        if (meta.isEmpty() || gmd >= meta.get()) {
            return;
        }

        alertaService.createAlertaSeNaoExistir(animal, TipoAlerta.GMD_ABAIXO_META, "GMD do animal abaixo da meta estabelecida: " + gmd + "kg/dia");
    }

    public void verificarQuedaPeso(Animal animal) {
        List<Pesagem> pesagens = pesagemRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (pesagens.size() < 3) {
            return;
        }

        double ultima = pesagens.get(pesagens.size() - 1).getPesoKg();
        double anterior = pesagens.get(pesagens.size() - 2).getPesoKg();
        double anteriorAnterior = pesagens.get(pesagens.size() - 3).getPesoKg();

        if (anterior >= anteriorAnterior || ultima >= anterior) {
            return;
        }

        alertaService.createAlertaSeNaoExistir(animal, TipoAlerta.QUEDA_PESO, "Animal apresentou queda de peso em duas pesagens consecutivas.");
    }

    public void verificarPesagemAtrasada(Animal animal) {
        Optional<Double> prazo = regraAlertaService.getParametroAtivo(animal, TipoAlerta.PESAGEM_ATRASADA);

        if (prazo.isEmpty()) {
            return;
        }

        List<Pesagem> pesagens = pesagemRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (pesagens.isEmpty()) {
            return;
        }

        long diasSemPesagem = ChronoUnit.DAYS.between(pesagens.get(pesagens.size() - 1).getData(), LocalDate.now());

        if (diasSemPesagem <= prazo.get()) {
            return;
        }

        alertaService.createAlertaSeNaoExistir(animal, TipoAlerta.PESAGEM_ATRASADA, "PESAGEM DO ANIMAL ESTÁ ATRASADA: Animal está há " + diasSemPesagem + " dias sem nova pesagem.");
    }
}
