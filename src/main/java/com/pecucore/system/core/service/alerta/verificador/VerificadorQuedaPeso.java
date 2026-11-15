package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.alerta.AlertaService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class VerificadorQuedaPeso implements VerificadorAlerta {

    @Autowired
    private PesagemService pesagemService;

    @Autowired
    private AlertaService alertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.QUEDA_PESO;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.PESAGEM_REGISTRADA);
    }

    @Override
    public void verificar(Animal animal) {
        List<Pesagem> pesagens = pesagemService.getHistoricoByAnimalId(animal.getId());

        if (pesagens.size() < 3) {
            return;
        }

        double ultima = pesagens.get(pesagens.size() - 1).getPesoKg();
        double anterior = pesagens.get(pesagens.size() - 2).getPesoKg();
        double anteriorAnterior = pesagens.get(pesagens.size() - 3).getPesoKg();

        if (anterior >= anteriorAnterior || ultima >= anterior) {
            return;
        }

        alertaService.createAlertaSeNaoExistir(animal, getTipo(),
                "Animal apresentou queda de peso em duas pesagens consecutivas.");
    }
}
