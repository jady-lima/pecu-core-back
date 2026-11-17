package com.pecucore.system.bovinos.service.alerta;

import com.pecucore.system.bovinos.model.TipoAlertaBovinos;
import com.pecucore.system.core.extensao.alerta.ProvedorTiposAlerta;
import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProvedorTiposAlertaBovinos implements ProvedorTiposAlerta {

    @Override
    public List<? extends TipoAlerta> getTipos() {
        return List.of(TipoAlertaBovinos.values());
    }
}
