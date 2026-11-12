package com.pecucore.system.core.ia;

import com.pecucore.system.core.dto.analise.ContextoAnaliseAnimalDTO;
import com.pecucore.system.core.dto.analise.ContextoAnaliseComparativaDTO;
import com.pecucore.system.core.dto.analise.RelatorioAnaliseDTO;

public interface AnaliseAnimalIAFacade {
    ResultadoAnaliseIA analisarIndividual(ContextoAnaliseAnimalDTO contexto);

    ResultadoAnaliseIA analisarComparativo(ContextoAnaliseComparativaDTO contexto);

    RelatorioAnaliseDTO lerRelatorio(String conteudo);
}
