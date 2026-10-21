package com.Pecucore.system.ia;

import com.Pecucore.system.dto.analise.ContextoAnaliseAnimalDTO;
import com.Pecucore.system.dto.analise.ContextoAnaliseComparativaDTO;
import com.Pecucore.system.dto.analise.RelatorioAnaliseDTO;

public interface AnaliseAnimalIAFacade {
    ResultadoAnaliseIA analisarIndividual(ContextoAnaliseAnimalDTO contexto);

    ResultadoAnaliseIA analisarComparativo(ContextoAnaliseComparativaDTO contexto);

    RelatorioAnaliseDTO lerRelatorio(String conteudo);
}
