package com.Pecucore.system.service;

import com.Pecucore.system.dto.analise.ContextoAnaliseAnimalDTO;
import com.Pecucore.system.dto.analise.ContextoAnaliseComparativaDTO;
import com.Pecucore.system.dto.analise.EstatisticasLoteDTO;
import com.Pecucore.system.dto.analise.ParLoteDTO;
import com.Pecucore.system.dto.analise.PosicaoAnimalLoteDTO;
import com.Pecucore.system.exception.DadosInsuficientesException;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Custo;
import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.model.StatusAnimal;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.AnimalRepository;
import com.Pecucore.system.repository.CustoRepository;
import com.Pecucore.system.repository.PesagemRepository;
import com.Pecucore.system.service.calculo.CalculadoraEstatisticasLote;
import com.Pecucore.system.service.calculo.GmdService;
import com.Pecucore.system.service.politica.PoliticaVisibilidadeFinanceira;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContextoComparativoService {

    private static final int MINIMO_PESAGENS = 2;
    private static final int MINIMO_ANIMAIS_COMPARACAO = 3;

    private record AnimalComparado(Animal animal, Double gmd) {
    }

    @Autowired
    private AnimalService animalService;

    @Autowired
    private ContextoAnaliseService contextoAnaliseService;

    @Autowired
    private GmdService gmdService;

    @Autowired
    private CalculadoraEstatisticasLote calculadoraEstatisticasLote;

    @Autowired
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private PesagemRepository pesagemRepository;

    @Autowired
    private CustoRepository custoRepository;

    @Transactional(readOnly = true)
    public ContextoAnaliseComparativaDTO montarComparativo(Long animalId, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalById(animalId, usuarioLogado);

        if (animal.getLote() == null) {
            throw new DadosInsuficientesException(
                    "O animal precisa estar vinculado a um lote para gerar a análise comparativa."
            );
        }

        ContextoAnaliseAnimalDTO contextoAnimal =
                contextoAnaliseService.montarIndividual(animalId, usuarioLogado);

        Double gmdAnimal = contextoAnimal.desempenho().gmdPeriodoKgDia();

        if (gmdAnimal == null) {
            throw new DadosInsuficientesException(
                    "Não foi possível calcular o GMD do animal. As pesagens precisam ter datas diferentes."
            );
        }

        Long loteId = animal.getLote().getId();
        List<AnimalComparado> demaisAnimais = montarDemaisAnimais(animalId, loteId);

        if (demaisAnimais.size() + 1 < MINIMO_ANIMAIS_COMPARACAO) {
            throw new DadosInsuficientesException(
                    "O lote precisa possuir pelo menos "
                            + MINIMO_ANIMAIS_COMPARACAO
                            + " animais ativos com "
                            + MINIMO_PESAGENS
                            + " pesagens ou mais para gerar a análise comparativa."
            );
        }

        List<AnimalComparado> comparados = new ArrayList<>(demaisAnimais);
        comparados.add(new AnimalComparado(animal, gmdAnimal));

        EstatisticasLoteDTO estatisticas = montarEstatisticasLote(
                loteId,
                contextoAnimal.desempenho().gmdMetaKgDia(),
                comparados,
                usuarioLogado
        );

        return new ContextoAnaliseComparativaDTO(
                contextoAnimal.dataReferencia(),
                contextoAnimal,
                montarPosicao(animal, gmdAnimal, demaisAnimais, estatisticas),
                estatisticas,
                demaisAnimais.stream()
                        .map(comparado -> new ParLoteDTO(
                                comparado.animal(),
                                comparado.gmd(),
                                contextoAnimal.dataReferencia()
                        ))
                        .toList()
        );
    }

    private List<AnimalComparado> montarDemaisAnimais(Long animalId, Long loteId) {
        Map<Long, List<Pesagem>> pesagensPorAnimal =
                pesagemRepository
                        .findByAnimalLoteIdAndAnimalStatusOrderByAnimalIdAscDataAscIdAsc(
                                loteId,
                                StatusAnimal.ATIVO
                        )
                        .stream()
                        .filter(pesagem -> !pesagem.getAnimal().getId().equals(animalId))
                        .collect(Collectors.groupingBy(
                                pesagem -> pesagem.getAnimal().getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        return pesagensPorAnimal.values()
                .stream()
                .filter(pesagens -> pesagens.size() >= MINIMO_PESAGENS)
                .map(pesagens -> new AnimalComparado(
                        pesagens.get(0).getAnimal(),
                        gmdService.calculateGmdHistorico(
                                pesagens.get(0),
                                pesagens.get(pesagens.size() - 1)
                        )
                ))
                .filter(comparado -> comparado.gmd() != null)
                .sorted(Comparator.comparing(AnimalComparado::gmd).reversed())
                .toList();
    }

    private EstatisticasLoteDTO montarEstatisticasLote(
            Long loteId,
            Double gmdMeta,
            List<AnimalComparado> comparados,
            Usuario usuarioLogado) {

        List<Double> gmds = comparados.stream()
                .map(AnimalComparado::gmd)
                .toList();

        List<Double> pesos = comparados.stream()
                .map(comparado -> comparado.animal().getPesoAtual())
                .toList();

        return new EstatisticasLoteDTO(
                (int) animalRepository.countByLoteIdAndStatus(loteId, StatusAnimal.ATIVO),
                comparados.size(),
                calculadoraEstatisticasLote.calcularMedia(gmds),
                calculadoraEstatisticasLote.calcularMediana(gmds),
                Collections.min(gmds),
                Collections.max(gmds),
                gmdMeta,
                calculadoraEstatisticasLote.calcularMedia(pesos),
                calcularCustoMedio(comparados, usuarioLogado)
        );
    }

    private PosicaoAnimalLoteDTO montarPosicao(
            Animal animal,
            Double gmdAnimal,
            List<AnimalComparado> demaisAnimais,
            EstatisticasLoteDTO estatisticas) {

        List<Double> gmdsDemaisAnimais = demaisAnimais.stream()
                .map(AnimalComparado::gmd)
                .toList();

        Double gmdMedio = estatisticas.gmdMedioKgDia();

        return new PosicaoAnimalLoteDTO(
                calculadoraEstatisticasLote.calcularPosicao(
                        gmdAnimal,
                        gmdsDemaisAnimais
                ),
                estatisticas.animaisComparados(),
                calculadoraEstatisticasLote.calcularPercentil(
                        gmdAnimal,
                        gmdsDemaisAnimais
                ),
                gmdAnimal - gmdMedio,
                calculadoraEstatisticasLote.calcularPercentualDaReferencia(
                        gmdAnimal,
                        gmdMedio
                ),
                animal.getPesoAtual() - estatisticas.pesoMedioKg()
        );
    }

    private Double calcularCustoMedio(
            List<AnimalComparado> comparados,
            Usuario usuarioLogado) {

        if (!politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuarioLogado)) {
            return null;
        }

        List<Long> animalIds = comparados.stream()
                .map(comparado -> comparado.animal().getId())
                .toList();

        double total = custoRepository.findByAnimalIdIn(animalIds)
                .stream()
                .mapToDouble(Custo::getValor)
                .sum();

        return total / animalIds.size();
    }
}