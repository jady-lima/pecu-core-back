package com.pecucore.system.core.extensao.agrupamento;

import com.pecucore.system.core.extensao.PontoFlexivel;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@PontoFlexivel
public interface PoliticaAgrupamento {

    List<FaseLote> getFases();

    Optional<Double> getMetaDaFase(String codigoFase);

    Optional<String> sugerirFase(Animal animal);

    /** Status de saída do rebanho aceitos pela aplicação (nunca ATIVO). */
    Set<StatusAnimal> getSaidasPossiveis();
}