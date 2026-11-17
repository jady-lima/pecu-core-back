package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.TipoAlertaNucleo;
import com.pecucore.system.core.extensao.alerta.VerificadorAlerta;
import com.pecucore.system.core.extensao.alerta.GatilhoAlerta;
import com.pecucore.system.core.extensao.alerta.OcorrenciaAlerta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import com.pecucore.system.core.service.pesagem.PesagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class VerificadorQuedaPeso implements VerificadorAlerta {

    @Autowired
    private PesagemService pesagemService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlertaNucleo.QUEDA_PESO;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.PESAGEM_REGISTRADA);
    }

    @Override
    public Optional<OcorrenciaAlerta> avaliar(Animal animal) {
        List<Pesagem> ultimas = pesagemService.getUltimasPesagens(animal.getId(), 3);

        if (ultimas.size() < 3) {
            return Optional.empty();
        }

        Pesagem ultima = ultimas.get(0);
        double anterior = ultimas.get(1).getPesoKg();
        double anteriorAnterior = ultimas.get(2).getPesoKg();

        if (anterior >= anteriorAnterior || ultima.getPesoKg() >= anterior) {
            return Optional.empty();
        }

        return Optional.of(new OcorrenciaAlerta("pesagem:" + ultima.getId(),
                "Queda de peso: o animal perdeu peso em duas pesagens consecutivas."));
    }
}
