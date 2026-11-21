package com.pecucore.system.core.dto.lote;

/**
 * Meta própria do lote como faixa: mínimo (metaDesempenho), máximo (metaDesempenhoMaxima) ou os dois.
 * A validação (pelo menos um limite; mínimo <= máximo) é feita pela Meta do núcleo, no LoteService.
 * Sem @Positive: em algumas aplicações a meta pode ser negativa (ex.: faixa de manutenção de peso).
 */
public record LoteMetaDesempenhoRequestDTO(
        Double metaDesempenho,
        Double metaDesempenhoMaxima
) {
}