package com.pecucore.system.bovinos.service.agrupamento;

import com.pecucore.system.bovinos.model.FinalidadeLote;
import com.pecucore.system.core.extensao.agrupamento.FaseLote;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class AgrupamentoBovinos implements PoliticaAgrupamento {

    private static final List<FaseLote> FASES = Arrays.stream(FinalidadeLote.values()).map(finalidade -> new FaseLote(finalidade.name(), finalidade.getDescricao())).toList();

    private static final Set<FinalidadeLote> FASES_POR_IDADE = EnumSet.of(FinalidadeLote.CRIA, FinalidadeLote.RECRIA, FinalidadeLote.ENGORDA);

    private static final int IDADE_MAXIMA_CRIA_MESES = 8;
    private static final int IDADE_MAXIMA_RECRIA_MESES = 18;

    private static final Set<StatusAnimal> SAIDAS =
            EnumSet.of(StatusAnimal.VENDIDO, StatusAnimal.OBITO, StatusAnimal.ABATIDO);

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

    @Override
    public Optional<String> sugerirFase(Animal animal) {
        if (!estaEmFasePorIdade(animal)) {
            return Optional.empty();
        }

        return animal.getIdadeMeses(LocalDate.now())
                .map(idadeMeses -> definirFasePorIdade(idadeMeses).name());
    }


    @Override
    public Set<StatusAnimal> getSaidasPossiveis() {
        return SAIDAS;
    }

    private boolean estaEmFasePorIdade(Animal animal) {
        return animal.getLote() != null && FinalidadeLote.deCodigo(animal.getLote().getFinalidade()).filter(FASES_POR_IDADE::contains).isPresent();
    }

    private FinalidadeLote definirFasePorIdade(long idadeMeses) {
        if (idadeMeses <= IDADE_MAXIMA_CRIA_MESES) {
            return FinalidadeLote.CRIA;
        }

        if (idadeMeses <= IDADE_MAXIMA_RECRIA_MESES) {
            return FinalidadeLote.RECRIA;
        }

        return FinalidadeLote.ENGORDA;
    }
}
