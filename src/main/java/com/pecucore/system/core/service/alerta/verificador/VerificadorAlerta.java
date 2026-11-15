package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;

import java.util.Set;

public interface VerificadorAlerta {

    TipoAlerta getTipo();

    Set<GatilhoAlerta> getGatilhos();

    void verificar(Animal animal);
}
