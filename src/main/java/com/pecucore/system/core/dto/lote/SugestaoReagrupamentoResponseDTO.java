package com.pecucore.system.core.dto.lote;

import com.pecucore.system.core.service.lote.SugestaoReagrupamento;

import java.time.LocalDate;
import java.util.List;

public record SugestaoReagrupamentoResponseDTO(
    Long animalId,
    int brinco,
    Long idadeMeses,
    Long loteAtualId,
    int numeroLoteAtual,
    String faseAtual,
    String faseSugerida,
    List<LoteDestinoDTO> lotesDestino
) {

    public record LoteDestinoDTO(Long loteId, int numero, long vagas) {
    }

    public SugestaoReagrupamentoResponseDTO(SugestaoReagrupamento sugestao) {
        this(
            sugestao.animal().getId(),
            sugestao.animal().getBrinco(),
            sugestao.animal().getIdadeMeses(LocalDate.now()).orElse(null),
            sugestao.animal().getLote().getId(),
            sugestao.animal().getLote().getNumero(),
            sugestao.animal().getLote().getFinalidade(),
            sugestao.faseSugerida(),
            sugestao.lotesDestino().stream()
                    .map(destino -> new LoteDestinoDTO(destino.lote().getId(), destino.lote().getNumero(), destino.vagas()))
                    .toList()
        );
    }
}
