package com.pecucore.system.equinos.service.agrupamento;

import com.pecucore.system.core.extensao.agrupamento.FaseLote;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.equinos.model.FaseEquinos;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Fases, metas padrão e saídas dos equinos.
 *
 * Metas padrão (variação de peso, kg/dia), para cavalos com cerca de 500 kg adultos:
 * - POTRO e JOVEM: ganho diário do NRC (2007), Nutrient Requirements of Horses, 6ª ed., reproduzido no
 *   MSD Veterinary Manual: 0,84 kg/dia aos 4 meses, 0,72 aos 6, 0,45 aos 12, 0,29 aos 18 e 0,18 aos 24 meses.
 *   As faixas abaixo cobrem esses valores.
 * - ADULTO: manutenção = peso estável. A tolerância de ±0,1 kg/dia é CRITÉRIO DO PROJETO, não da literatura.
 * - REPRODUCAO e OBSERVACAO: sem meta padrão (gestação e lactação variam muito); o produtor define a do lote.
 * Os valores mudam com o porte adulto (pôneis, cavalos de tração). A meta cadastrada por fase
 * (/metas-desempenho) e a meta própria do lote têm prioridade sobre estes padrões.
 *
 * Idades: potro até 12 meses e jovem até 36 meses; o NRC trata como adultos os cavalos acima de 3 a 4 anos.
 */
@Component
public class AgrupamentoEquinos implements PoliticaAgrupamento {

    private static final List<FaseLote> FASES = Arrays.stream(FaseEquinos.values())
            .map(fase -> new FaseLote(fase.name(), fase.getDescricao()))
            .toList();

    private static final Map<FaseEquinos, Meta> METAS_PADRAO = Map.of(
            FaseEquinos.POTRO, Meta.faixa(0.45, 0.85),
            FaseEquinos.JOVEM, Meta.faixa(0.15, 0.45),
            FaseEquinos.ADULTO, Meta.faixa(-0.1, 0.1)
    );

    private static final Set<FaseEquinos> FASES_POR_IDADE =
            EnumSet.of(FaseEquinos.POTRO, FaseEquinos.JOVEM, FaseEquinos.ADULTO);

    private static final int IDADE_MAXIMA_POTRO_MESES = 12;
    private static final int IDADE_MAXIMA_JOVEM_MESES = 36;

    /** Escopo desta aplicação: abate não é uma saída prevista para equinos. */
    private static final Set<StatusAnimal> SAIDAS = EnumSet.of(StatusAnimal.VENDIDO, StatusAnimal.OBITO);

    @Override
    public List<FaseLote> getFases() {
        return FASES;
    }

    @Override
    public Optional<Meta> getMetaDaFase(String codigoFase) {
        return FaseEquinos.deCodigo(codigoFase).map(METAS_PADRAO::get);
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
        return animal.getLote() != null
                && FaseEquinos.deCodigo(animal.getLote().getFinalidade()).filter(FASES_POR_IDADE::contains).isPresent();
    }

    private FaseEquinos definirFasePorIdade(long idadeMeses) {
        if (idadeMeses <= IDADE_MAXIMA_POTRO_MESES) {
            return FaseEquinos.POTRO;
        }

        if (idadeMeses <= IDADE_MAXIMA_JOVEM_MESES) {
            return FaseEquinos.JOVEM;
        }

        return FaseEquinos.ADULTO;
    }
}