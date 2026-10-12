package com.Pecucore.system.dto.analise;

import java.time.LocalDate;
import java.util.List;

public record ContextoAnaliseAnimalDTO(
        LocalDate dataReferencia,
        DadosAnimalDTO animal,
        List<PesagemContextoDTO> pesagens,
        List<AlimentacaoContextoDTO> alimentacoes,
        List<VacinacaoContextoDTO> vacinacoes,
        List<MedicamentoContextoDTO> medicamentos,
        ResumoCustoDTO custos
) {
}
