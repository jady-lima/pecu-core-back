package com.pecucore.system.core.service.lote;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Lote;

import java.util.List;

public record SugestaoReagrupamento(
    Animal animal,
    String faseSugerida,
    List<LoteDestino> lotesDestino
) {
    public record LoteDestino(Lote lote, long vagas) {
    }
}
