package com.pecucore.system.bovinos.service.agrupamento;

import com.pecucore.system.bovinos.model.FinalidadeLote;
import com.pecucore.system.core.extensao.agrupamento.FaseLote;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class AgrupamentoBovinos implements PoliticaAgrupamento {

    private static final List<FaseLote> FASES = Arrays.stream(FinalidadeLote.values())
            .map(finalidade -> new FaseLote(finalidade.name(), finalidade.getDescricao()))
            .toList();

    @Autowired
    private MetaDesempenhoService metaDesempenhoService;

    @Override
    public List<FaseLote> getFases() {
        return FASES;
    }

    @Override
    public Optional<Double> getMetaDaFase(String codigoFase) {
        return FinalidadeLote.deCodigo(codigoFase)
                .flatMap(metaDesempenhoService::getGmdEsperadoByFase);
    }
}
