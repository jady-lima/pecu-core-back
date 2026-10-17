package com.Pecucore.system.ia;

import com.Pecucore.system.dto.analise.ContextoAnaliseAnimalDTO;
import com.Pecucore.system.dto.analise.RelatorioAnaliseDTO;

public interface AnaliseAnimalIAFacade {
    ResultadoAnaliseIA analisar(ContextoAnaliseAnimalDTO contexto);

    RelatorioAnaliseDTO lerRelatorio(String conteudo);
}
