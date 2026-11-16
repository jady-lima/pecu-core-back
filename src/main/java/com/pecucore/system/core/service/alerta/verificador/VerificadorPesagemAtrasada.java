package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.TipoAlerta;
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
        return TipoAlerta.PESAGEM_ATRASADA;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.PESAGEM_REGISTRADA, GatilhoAlerta.ROTINA_DIARIA);
    }

    @Override
    public Optional<OcorrenciaAlerta> avaliar(Animal animal) {
        Optional<Double> prazo = regraAlertaService.getParametroAtivo(animal, getTipo());
        Optional<Pesagem> ultima = pesagemService.getUltimaPesagem(animal.getId());

        if (prazo.isEmpty() || ultima.isEmpty()) {
            return Optional.empty();
        }

        long diasSemPesagem = ChronoUnit.DAYS.between(ultima.get().getData(), LocalDate.now());

        if (diasSemPesagem <= prazo.get()) {
            return Optional.empty();
        }

        return Optional.of(new OcorrenciaAlerta("pesagem:" + ultima.get().getId(),
                "Pesagem atrasada: o animal está há " + diasSemPesagem + " dias sem nova pesagem."));
    }
}
