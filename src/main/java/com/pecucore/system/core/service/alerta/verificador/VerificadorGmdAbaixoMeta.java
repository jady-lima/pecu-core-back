package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.alerta.AlertaService;
import com.pecucore.system.core.service.alerta.RegraAlertaService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class VerificadorGmdAbaixoMeta implements VerificadorAlerta {

    @Autowired
    private PesagemService pesagemService;

    @Autowired
    private RegraAlertaService regraAlertaService;

    @Autowired
    private AlertaService alertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.GMD_ABAIXO_META;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.PESAGEM_REGISTRADA);
    }

    @Override
    public void verificar(Animal animal) {
        Optional<Double> meta = regraAlertaService.getParametroAtivo(animal, getTipo());

        if (meta.isEmpty()) {
            return;
        }

        List<Pesagem> pesagens = pesagemService.getHistoricoByAnimalId(animal.getId());

        if (pesagens.isEmpty()) {
            return;
        }

        Double gmd = pesagens.get(pesagens.size() - 1).getGmdCalculado();

        if (gmd == null || gmd >= meta.get()) {
            return;
        }

        alertaService.createAlertaSeNaoExistir(animal, getTipo(),
                "GMD do animal abaixo da meta estabelecida: " + gmd + "kg/dia");
    }
}
