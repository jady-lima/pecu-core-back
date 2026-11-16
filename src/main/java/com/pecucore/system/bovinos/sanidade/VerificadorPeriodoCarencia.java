package com.pecucore.system.bovinos.sanidade;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.alerta.AlertaService;
import com.pecucore.system.core.service.alerta.verificador.GatilhoAlerta;
import com.pecucore.system.core.service.alerta.verificador.VerificadorAlerta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class VerificadorPeriodoCarencia implements VerificadorAlerta {

    @Autowired
    private CarenciaService carenciaService;

    @Autowired
    private AlertaService alertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.PERIODO_CARENCIA;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(
            GatilhoAlerta.VACINACAO_REGISTRADA,
            GatilhoAlerta.MEDICAMENTO_REGISTRADO,
            GatilhoAlerta.ROTINA_DIARIA
        );
    }

    @Override
    public void verificar(Animal animal) {
        carenciaService.getDataFimCarencia(animal.getId())
            .ifPresent(dataFim -> alertaService.createAlertaSeNaoExistir(animal, getTipo(), "Animal ainda está em período de carência (vacinas ou medicamentos). " + "Animal estará em período de carência até " + dataFim)
        );
    }
}
