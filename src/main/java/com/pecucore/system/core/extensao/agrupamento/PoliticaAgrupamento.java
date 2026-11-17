package com.pecucore.system.core.extensao.agrupamento;

import com.pecucore.system.core.extensao.PontoFlexivel;

import java.util.List;
import java.util.Optional;

@PontoFlexivel
public interface PoliticaAgrupamento {

    List<FaseLote> getFases();

    Optional<Double> getMetaDaFase(String codigoFase);
}
