package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.alerta.AlertaService;
import com.pecucore.system.core.service.alerta.RegraAlertaService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class VerificadorPesagemAtrasada implements VerificadorAlerta {

    @Autowired
    private PesagemService pesagemService;

    @Autowired
    private RegraAlertaService regraAlertaService;

    @Autowired
    private AlertaService alertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.PESAGEM_ATRASADA;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.ROTINA_DIARIA);
    }

    @Override
    public void verificar(Animal animal) {
        Optional<Double> prazo = regraAlertaService.getParametroAtivo(animal, getTipo());

        if (prazo.isEmpty()) {
            return;
        }

        List<Pesagem> pesagens = pesagemService.getHistoricoByAnimalId(animal.getId());

        if (pesagens.isEmpty()) {
            return;
        }

        long diasSemPesagem = ChronoUnit.DAYS.between(
                pesagens.get(pesagens.size() - 1).getData(), LocalDate.now());

        if (diasSemPesagem <= prazo.get()) {
            return;
        }

        alertaService.createAlertaSeNaoExistir(animal, getTipo(),
                "PESAGEM DO ANIMAL ESTÁ ATRASADA: Animal está há "
                        + diasSemPesagem + " dias sem nova pesagem.");
    }
}
