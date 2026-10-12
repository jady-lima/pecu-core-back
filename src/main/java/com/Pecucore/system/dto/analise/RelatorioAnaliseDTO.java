package com.Pecucore.system.dto.analise;

import com.Pecucore.system.model.SituacaoGeralAnimal;

import java.util.List;

public record RelatorioAnaliseDTO(
        SituacaoGeralAnimal situacaoGeral,
        String resumo,
        String evolucaoDesempenho,
        List<String> pontosAtencao,
        List<String> possiveisFatores,
        List<String> recomendacoes,
        List<String> acompanhamento,
        List<String> limitacoesDados
) {
}
