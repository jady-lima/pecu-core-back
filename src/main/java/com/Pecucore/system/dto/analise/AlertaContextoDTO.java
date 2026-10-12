package com.Pecucore.system.dto.analise;

import com.Pecucore.system.model.Alerta;
import com.Pecucore.system.model.SeveridadeAlerta;
import com.Pecucore.system.model.StatusAlerta;
import com.Pecucore.system.model.TipoAlerta;

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
