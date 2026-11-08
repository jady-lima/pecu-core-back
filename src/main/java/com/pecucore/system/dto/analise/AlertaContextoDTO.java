package com.pecucore.system.dto.analise;

import com.pecucore.system.model.Alerta;
import com.pecucore.system.model.SeveridadeAlerta;
import com.pecucore.system.model.StatusAlerta;
import com.pecucore.system.model.TipoAlerta;

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
