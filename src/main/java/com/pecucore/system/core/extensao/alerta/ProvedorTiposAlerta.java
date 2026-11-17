package com.pecucore.system.core.extensao.alerta;

import java.util.List;

public interface ProvedorTiposAlerta {

    List<? extends TipoAlerta> getTipos();
}
