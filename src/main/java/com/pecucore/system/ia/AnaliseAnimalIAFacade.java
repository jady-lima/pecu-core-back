package com.pecucore.system.ia;

import com.pecucore.system.dto.analise.ContextoAnaliseAnimalDTO;
import com.pecucore.system.dto.analise.ContextoAnaliseComparativaDTO;
import com.pecucore.system.dto.analise.RelatorioAnaliseDTO;

public interface AnaliseAnimalIAFacade {
    ResultadoAnaliseIA analisarIndividual(ContextoAnaliseAnimalDTO contexto);

    ResultadoAnaliseIA analisarComparativo(ContextoAnaliseComparativaDTO contexto);

    RelatorioAnaliseDTO lerRelatorio(String conteudo);
}
