package com.pecucore.system.bovinos.service.alerta.verificador;

import com.pecucore.system.bovinos.model.TipoAlertaBovinos;
import com.pecucore.system.core.service.sanidade.CarenciaService;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import com.pecucore.system.core.extensao.alerta.GatilhoAlerta;
import com.pecucore.system.core.service.alerta.verificador.FormatadorAlerta;
import com.pecucore.system.core.extensao.alerta.OcorrenciaAlerta;
import com.pecucore.system.core.extensao.alerta.VerificadorAlerta;
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
        return TipoAlertaBovinos.PERIODO_CARENCIA;
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
                .map(dataFim -> new OcorrenciaAlerta("carencia-ate:" + dataFim, "Período de carência: o animal está em carência (vacinas ou medicamentos) até " + FormatadorAlerta.data(dataFim) + "."));
    }
}
