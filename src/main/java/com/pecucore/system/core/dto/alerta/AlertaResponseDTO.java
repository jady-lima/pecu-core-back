package com.pecucore.system.core.dto.alerta;

import com.pecucore.system.core.model.Alerta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.SeveridadeAlerta;
import com.pecucore.system.core.model.StatusAlerta;
import com.pecucore.system.core.model.TipoAlerta;

import java.time.LocalDateTime;

public record AlertaResponseDTO(
        Long id,
        TipoAlerta tipo,
        SeveridadeAlerta severidade,
        String mensagem,
        StatusAlerta status,
        LocalDateTime dataCriacao,
        AnimalResumoDTO animal
) {

    public record AnimalResumoDTO(Long id, int brinco) {
    }

    public AlertaResponseDTO(Alerta alerta) {
        this(
                alerta.getId(),
                alerta.getTipo(),
                alerta.getSeveridade(),
                alerta.getMensagem(),
                alerta.getStatus(),
                alerta.getDataCriacao(),
                resumir(alerta.getAnimal())
        );
    }

    private static AnimalResumoDTO resumir(Animal animal) {
        return animal != null ? new AnimalResumoDTO(animal.getId(), animal.getBrinco()) : null;
    }
}