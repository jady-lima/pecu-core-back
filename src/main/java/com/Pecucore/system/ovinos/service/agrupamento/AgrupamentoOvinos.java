package com.Pecucore.system.ovinos.service.agrupamento;


import com.Pecucore.system.ovinos.model.FinalidadeLoteOvino;
import com.pecucore.system.core.extensao.agrupamento.FaseLote;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Component
public class AgrupamentoOvinos implements PoliticaAgrupamento {
    private static final List<FaseLote> FASES = Arrays.stream(com.Pecucore.system.ovinos.model.FinalidadeLoteOvino.values()).map(finalidade -> new FaseLote(finalidade.name(), finalidade.getDescricao())).toList();

    private static final Set<FinalidadeLoteOvino> FASES_POR_IDADE = EnumSet.of(FinalidadeLoteOvino.CORDEIRO,FinalidadeLoteOvino.RECRIA, FinalidadeLoteOvino.TERMINACAO);

    private static final int IDADE_MAXIMA_CORDEIRO_MESES = 3;
    private static final int IDADE_MAXIMA_RECRIA_MESES = 5;

    private static final Set<StatusAnimal> SAIDAS =
            EnumSet.of(StatusAnimal.VENDIDO, StatusAnimal.OBITO, StatusAnimal.ABATIDO);

    @Override
    public List<FaseLote> getFases() {
        return FASES;
    }

    /**
     * Os Ovinos não possuem a meta padrão no código, As metas por fases são cadastradas pelo admnistrador.
     */

    @Override
    public Optional<Meta> getMetaDaFase(String codigoFase) {
        return Optional.empty();
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
        return animal.getLote() != null && FinalidadeLoteOvino.deCodigo(animal.getLote().getFinalidade()).filter(FASES_POR_IDADE::contains).isPresent();
    }

    private FinalidadeLoteOvino definirFasePorIdade(long idadeMeses) {
        if (idadeMeses <= IDADE_MAXIMA_CORDEIRO_MESES) {
            return FinalidadeLoteOvino.CORDEIRO;
        }

        if (idadeMeses <= IDADE_MAXIMA_RECRIA_MESES) {
            return FinalidadeLoteOvino.RECRIA;
        }

        return FinalidadeLoteOvino.TERMINACAO;
    }
}

