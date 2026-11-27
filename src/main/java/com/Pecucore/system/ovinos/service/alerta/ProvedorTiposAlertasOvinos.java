package com.Pecucore.system.ovinos.service.alerta;


import com.Pecucore.system.ovinos.model.TipoAlertaOvinos;
import com.pecucore.system.core.extensao.alerta.ProvedorTiposAlerta;
import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProvedorTiposAlertasOvinos implements ProvedorTiposAlerta {

    @Override
    public List<? extends TipoAlerta> getTipos() {
        return List.of(TipoAlertaOvinos.values());
    }
}
