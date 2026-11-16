package com.pecucore.system.bovinos.sanidade;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.alerta.verificador.GatilhoAlerta;
import com.pecucore.system.core.service.alerta.verificador.OcorrenciaAlerta;
import com.pecucore.system.core.service.alerta.verificador.VerificadorAlerta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class VerificadorPeriodoCarencia implements VerificadorAlerta {

    @Autowired
    private CarenciaService carenciaService;

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
    public Optional<OcorrenciaAlerta> avaliar(Animal animal) {
        return carenciaService.getDataFimCarencia(animal.getId())
                .map(dataFim -> new OcorrenciaAlerta("carencia-ate:" + dataFim,
                    "Animal ainda está em período de carência (vacinas ou medicamentos). " + "Animal estará em período de carência até " + dataFim));
    }
}
