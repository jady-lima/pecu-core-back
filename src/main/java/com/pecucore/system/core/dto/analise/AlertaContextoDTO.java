package com.pecucore.system.core.dto.analise;

import com.pecucore.system.core.model.Alerta;
import com.pecucore.system.core.model.SeveridadeAlerta;
import com.pecucore.system.core.model.StatusAlerta;
import com.pecucore.system.core.model.TipoAlerta;

import java.time.LocalDateTime;

public record AlertaContextoDTO(
        LocalDateTime dataCriacao,
        TipoAlerta tipo,
        SeveridadeAlerta severidade,
        StatusAlerta status,
        String mensagem
) {

    public AlertaContextoDTO(Alerta alerta) {
        this(
                alerta.getDataCriacao(),
                alerta.getTipo(),
                alerta.getSeveridade(),
                alerta.getStatus(),
                alerta.getMensagem()
        );
    }
}
