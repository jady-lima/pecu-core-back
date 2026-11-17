package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.TipoAlertaNucleo;
import com.pecucore.system.core.extensao.alerta.VerificadorAlerta;
import com.pecucore.system.core.extensao.alerta.GatilhoAlerta;
import com.pecucore.system.core.extensao.alerta.OcorrenciaAlerta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import com.pecucore.system.core.service.alerta.RegraAlertaService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

@Component
public class VerificadorPesagemAtrasada implements VerificadorAlerta {

    @Autowired
    private PesagemService pesagemService;

    @Autowired
    private RegraAlertaService regraAlertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlertaNucleo.PESAGEM_ATRASADA;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.PESAGEM_REGISTRADA, GatilhoAlerta.ROTINA_DIARIA);
    }

    @Override
    public Optional<OcorrenciaAlerta> avaliar(Animal animal) {
        if (regraAlertaService.isAlertaDesativado(animal, getTipo())) {
            return Optional.empty();
        }

        Optional<Pesagem> ultima = pesagemService.getUltimaPesagem(animal.getId());

        if (ultima.isEmpty()) {
            return Optional.empty();
        }

        double prazo = regraAlertaService.getParametroAtivo(animal, getTipo())
                .orElseGet(() -> (double) pesagemService.getIntervaloRecomendadoDias(animal));

        long diasSemPesagem = ChronoUnit.DAYS.between(ultima.get().getData(), LocalDate.now());

        if (diasSemPesagem <= prazo) {
            return Optional.empty();
        }

        return Optional.of(new OcorrenciaAlerta("pesagem:" + ultima.get().getId(),
                "Pesagem atrasada: o animal está há " + diasSemPesagem + " dias sem nova pesagem."));
    }
}
