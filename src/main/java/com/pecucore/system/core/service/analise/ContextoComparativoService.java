package com.pecucore.system.core.service.analise;

import com.pecucore.system.core.dto.analise.ContextoAnaliseAnimalDTO;
import com.pecucore.system.core.dto.analise.ContextoAnaliseComparativaDTO;
import com.pecucore.system.core.dto.analise.EstatisticasLoteDTO;
import com.pecucore.system.core.dto.analise.ParLoteDTO;
import com.pecucore.system.core.dto.analise.PesagemContextoDTO;
import com.pecucore.system.core.dto.analise.PosicaoAnimalLoteDTO;
import com.pecucore.system.core.exception.DadosInsuficientesException;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Custo;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.animal.AnimalConsultaService;
import com.pecucore.system.core.service.animal.AnimalService;
import com.pecucore.system.core.service.custo.CustoService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import com.pecucore.system.core.service.calculo.CalculadoraEstatisticasLote;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.service.politica.PoliticaVisibilidadeFinanceira;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private static final double COBERTURA_MINIMA_JANELA = 0.5;

    private record AnimalComparado(Animal animal, Double indice) {
    }

    private record JanelaComparacao(LocalDate inicio, LocalDate fim) {
        long dias() {
            return ChronoUnit.DAYS.between(inicio, fim);
        }

        boolean contem(LocalDate data) {
            return !data.isBefore(inicio) && !data.isAfter(fim);
        }
    }

    @Autowired
    private AnimalService animalService;

    @Autowired
    private AnimalConsultaService animalConsultaService;

    @Autowired
    private ContextoAnaliseService contextoAnaliseService;

    @Autowired
    private IndicadorDesempenho indicadorDesempenho;

    @Autowired
    private CalculadoraEstatisticasLote calculadoraEstatisticasLote;

    @Autowired
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira;

    @Autowired
    private PesagemService pesagemService;

    @Autowired
    private CustoService custoService;

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

        Double indiceAnimal = contextoAnimal.desempenho().indicePeriodo();

        if (indiceAnimal == null) {
            throw new DadosInsuficientesException(
                    "Não foi possível calcular o " + indicadorDesempenho.getNome()
                            + " do animal. As pesagens precisam ter datas diferentes."
            );
        }

        JanelaComparacao janela = montarJanela(contextoAnimal);

        Long loteId = animal.getLote().getId();
        List<AnimalComparado> demaisAnimais = montarDemaisAnimais(animalId, loteId, janela);

        if (demaisAnimais.size() + 1 < MINIMO_ANIMAIS_COMPARACAO) {
            throw new DadosInsuficientesException(
                    "O lote precisa possuir pelo menos "
                            + MINIMO_ANIMAIS_COMPARACAO
                            + " animais ativos com "
                            + MINIMO_PESAGENS
                            + " pesagens ou mais entre "
                            + janela.inicio()
                            + " e "
                            + janela.fim()
                            + " para gerar a análise comparativa."
            );
        }

        List<AnimalComparado> comparados = new ArrayList<>(demaisAnimais);
        comparados.add(new AnimalComparado(animal, indiceAnimal));

        EstatisticasLoteDTO estatisticas = montarEstatisticasLote(
                loteId,
                contextoAnimal.desempenho().indiceMeta(),
                comparados,
                usuarioLogado
        );

        return new ContextoAnaliseComparativaDTO(
                contextoAnimal.dataReferencia(),
                contextoAnimal,
                montarPosicao(animal, indiceAnimal, demaisAnimais, estatisticas),
                estatisticas,
                demaisAnimais.stream()
                        .map(comparado -> new ParLoteDTO(
                                comparado.animal(),
                                comparado.indice(),
                                contextoAnimal.dataReferencia()
                        ))
                        .toList()
        );
    }

    private JanelaComparacao montarJanela(ContextoAnaliseAnimalDTO contextoAnimal) {
        List<PesagemContextoDTO> pesagens = contextoAnimal.pesagens();

        return new JanelaComparacao(
                pesagens.get(0).data(),
                pesagens.get(pesagens.size() - 1).data()
        );
    }

    private List<AnimalComparado> montarDemaisAnimais(Long animalId, Long loteId, JanelaComparacao janela) {
        Map<Long, List<Pesagem>> pesagensPorAnimal =
                pesagemService
                        .getPesagensAnimaisAtivosByLoteId(loteId)
                        .stream()
                        .filter(pesagem -> !pesagem.getAnimal().getId().equals(animalId))
                        .filter(pesagem -> janela.contem(pesagem.getData()))
                        .collect(Collectors.groupingBy(
                                pesagem -> pesagem.getAnimal().getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        return pesagensPorAnimal.values()
                .stream()
                .filter(pesagens -> pesagens.size() >= MINIMO_PESAGENS)
                .filter(pesagens -> cobreJanela(pesagens, janela))
                .map(pesagens -> new AnimalComparado(
                        pesagens.get(0).getAnimal(),
                        indicadorDesempenho.calcularHistorico(
                                pesagens.get(0),
                                pesagens.get(pesagens.size() - 1)
                        )
                ))
                .filter(comparado -> comparado.indice() != null)
                .sorted(Comparator.comparing(AnimalComparado::indice).reversed())
                .toList();
    }

    private boolean cobreJanela(List<Pesagem> pesagens, JanelaComparacao janela) {
        long diasCobertos = ChronoUnit.DAYS.between(
                pesagens.get(0).getData(),
                pesagens.get(pesagens.size() - 1).getData()
        );

        return diasCobertos >= janela.dias() * COBERTURA_MINIMA_JANELA;
    }

    private EstatisticasLoteDTO montarEstatisticasLote(
            Long loteId,
            Double indiceMeta,
            List<AnimalComparado> comparados,
            Usuario usuarioLogado) {

        List<Double> indices = comparados.stream()
                .map(AnimalComparado::indice)
                .toList();

        List<Double> pesos = comparados.stream()
                .map(comparado -> comparado.animal().getPesoAtual())
                .toList();

        return new EstatisticasLoteDTO(
                (int) animalConsultaService.countAnimaisAtivosByLoteId(loteId),
                comparados.size(),
                calculadoraEstatisticasLote.calcularMedia(indices),
                calculadoraEstatisticasLote.calcularMediana(indices),
                Collections.min(indices),
                Collections.max(indices),
                indiceMeta,
                calculadoraEstatisticasLote.calcularMedia(pesos),
                calcularCustoMedio(comparados, usuarioLogado)
        );
    }

    private PosicaoAnimalLoteDTO montarPosicao(
            Animal animal,
            Double indiceAnimal,
            List<AnimalComparado> demaisAnimais,
            EstatisticasLoteDTO estatisticas) {

        List<Double> indicesDemaisAnimais = demaisAnimais.stream()
                .map(AnimalComparado::indice)
                .toList();

        Double indiceMedio = estatisticas.indiceMedio();

        return new PosicaoAnimalLoteDTO(
                calculadoraEstatisticasLote.calcularPosicao(
                        indiceAnimal,
                        indicesDemaisAnimais
                ),
                estatisticas.animaisComparados(),
                calculadoraEstatisticasLote.calcularPercentil(
                        indiceAnimal,
                        indicesDemaisAnimais
                ),
                indiceAnimal - indiceMedio,
                calculadoraEstatisticasLote.calcularPercentualDaReferencia(
                        indiceAnimal,
                        indiceMedio
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

        double total = custoService.getCustosByAnimalIds(animalIds)
                .stream()
                .mapToDouble(Custo::getValor)
                .sum();

        return total / animalIds.size();
    }
}