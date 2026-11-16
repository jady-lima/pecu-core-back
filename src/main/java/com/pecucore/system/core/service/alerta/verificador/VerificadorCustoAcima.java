package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.alerta.RegraAlertaService;
import com.pecucore.system.core.service.custo.CustoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class VerificadorCustoAcima implements VerificadorAlerta {

    @Autowired
    private CustoService custoService;

    @Autowired
    private RegraAlertaService regraAlertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.CUSTO_ACIMA;
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

        double custoTotal = custoService.getCustoTotalByAnimalId(animal.getId());

        if (custoTotal <= limite.get()) {
            return Optional.empty();
        }

        return Optional.of(new OcorrenciaAlerta("limite:" + limite.get(),
                "O animal está com custo acumulado de R$ " + custoTotal
                        + ", acima do limite estabelecido de R$ " + limite.get() + "."));
    }
}
