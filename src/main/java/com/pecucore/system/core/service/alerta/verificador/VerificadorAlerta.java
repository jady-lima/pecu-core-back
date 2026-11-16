package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;

import java.util.Optional;
import java.util.Set;

public interface VerificadorAlerta {

    TipoAlerta getTipo();

    Set<GatilhoAlerta> getGatilhos();


    Optional<OcorrenciaAlerta> avaliar(Animal animal);
}
