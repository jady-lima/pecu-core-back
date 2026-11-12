package com.pecucore.system.core.dto.analise;

import com.pecucore.system.core.model.SituacaoGeralAnimal;

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
