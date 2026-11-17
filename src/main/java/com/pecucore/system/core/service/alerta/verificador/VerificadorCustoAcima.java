package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.TipoAlertaNucleo;
import com.pecucore.system.core.extensao.alerta.VerificadorAlerta;
import com.pecucore.system.core.extensao.alerta.GatilhoAlerta;
import com.pecucore.system.core.extensao.alerta.OcorrenciaAlerta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import com.pecucore.system.core.service.alerta.RegraAlertaService;
import com.pecucore.system.core.service.custo.RateioCustoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class VerificadorCustoAcima implements VerificadorAlerta {

    @Autowired
    private RateioCustoService rateioCustoService;

    @Autowired
    private RegraAlertaService regraAlertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlertaNucleo.CUSTO_ACIMA;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.CUSTO_REGISTRADO, GatilhoAlerta.ROTINA_DIARIA);
    }

    @Override
    public Optional<OcorrenciaAlerta> avaliar(Animal animal) {
        Optional<Double> limite = regraAlertaService.getParametroAtivo(animal, getTipo());

        if (limite.isEmpty()) {
            return Optional.empty();
        }

        double custoTotal = rateioCustoService.calcularRateio(animal).total();

        if (custoTotal <= limite.get()) {
            return Optional.empty();
        }

        return Optional.of(new OcorrenciaAlerta("limite:" + limite.get(),
                "Custo acima do limite: o animal acumula " + FormatadorAlerta.moeda(custoTotal)
                        + ", acima do limite de " + FormatadorAlerta.moeda(limite.get()) + "."));
    }
}
