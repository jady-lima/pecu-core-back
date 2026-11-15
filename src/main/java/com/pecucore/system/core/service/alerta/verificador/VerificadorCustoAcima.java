package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.alerta.AlertaService;
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

    @Autowired
    private AlertaService alertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.CUSTO_ACIMA;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.CUSTO_REGISTRADO);
    }

    @Override
    public void verificar(Animal animal) {
        Optional<Double> limite = regraAlertaService.getParametroAtivo(animal, getTipo());

        if (limite.isEmpty()) {
            return;
        }

        double custoTotal = custoService.getCustoTotalByAnimalId(animal.getId());

        if (custoTotal <= limite.get()) {
            return;
        }

        alertaService.createAlertaSeNaoExistir(animal, getTipo(), "O animal está com custo acumulado de R$ " + custoTotal + ", acima do limite estabelecido de R$ " + limite.get() + ".");
    }
}
