package com.pecucore.system.core.extensao.agrupamento;

import com.pecucore.system.core.extensao.PontoFlexivel;
import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@PontoFlexivel
public interface PoliticaAgrupamento {

    List<FaseLote> getFases();

    /**
     * Meta padrão da aplicação para a fase (valor de referência, com fonte citada na implementação).
     * Só é usada quando o lote não tem meta própria e não há meta cadastrada para a fase (MetaFaseService).
     */
    Optional<Meta> getMetaDaFase(String codigoFase);


    Optional<String> sugerirFase(Animal animal);

    /** Status de saída do rebanho aceitos pela aplicação (nunca ATIVO). */
    Set<StatusAnimal> getSaidasPossiveis();
}