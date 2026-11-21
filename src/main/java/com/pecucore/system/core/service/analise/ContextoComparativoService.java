package com.pecucore.system.core.service.analise;

import com.pecucore.system.core.dto.analise.ContextoAnaliseAnimalDTO;
import com.pecucore.system.core.dto.analise.ContextoAnaliseComparativaDTO;
import com.pecucore.system.core.dto.analise.DesempenhoContextoDTO;
import com.pecucore.system.core.dto.analise.EstatisticasLoteDTO;
import com.pecucore.system.core.dto.analise.ParLoteDTO;
import com.pecucore.system.core.dto.analise.PesagemContextoDTO;
import com.pecucore.system.core.dto.analise.PosicaoAnimalLoteDTO;
import com.pecucore.system.core.exception.DadosInsuficientesException;
import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.animal.AnimalConsultaService;
import com.pecucore.system.core.service.animal.AnimalService;
import com.pecucore.system.core.service.custo.RateioCustoService;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ContextoComparativoService {

    private static final int MINIMO_ANIMAIS_COMPARACAO = 3;
    private static final double COBERTURA_MINIMA_JANELA = 0.5;

    /** indice = valor do indicador no período; pontuacao = critério de ranking definido pela aplicação. */
    private record AnimalComparado(Animal animal, Double indice, double pontuacao) {
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
    private RateioCustoService rateioCustoService;

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
        Meta meta = montarMeta(contextoAnimal.desempenho());

        Long loteId = animal.getLote().getId();
        List<AnimalComparado> demaisAnimais = montarDemaisAnimais(animalId, loteId, janela, meta);

        if (demaisAnimais.size() + 1 < MINIMO_ANIMAIS_COMPARACAO) {
            throw new DadosInsuficientesException(
                    "O lote precisa possuir pelo menos "
                            + MINIMO_ANIMAIS_COMPARACAO
                            + " animais ativos com "
                            + RequisitosAnalise.MINIMO_PESAGENS
                            + " pesagens ou mais entre "
                            + janela.inicio()
                            + " e "
                            + janela.fim()
                            + " para gerar a análise comparativa."
            );
        }

        AnimalComparado animalAnalisado = comparar(animal, indiceAnimal, meta);

        List<AnimalComparado> comparados = new ArrayList<>(demaisAnimais);
        comparados.add(animalAnalisado);

        EstatisticasLoteDTO estatisticas = montarEstatisticasLote(
                loteId,
                contextoAnimal.desempenho(),
                comparados,
                usuarioLogado
        );

        return new ContextoAnaliseComparativaDTO(
                contextoAnimal.dataReferencia(),
                contextoAnimal,
                montarPosicao(animalAnalisado, demaisAnimais, estatisticas),
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

    /** Meta do lote do animal analisado (nula quando não há meta), já calculada pelo ContextoAnaliseService. */
    private static Meta montarMeta(DesempenhoContextoDTO desempenho) {
        if (desempenho.indiceMeta() == null && desempenho.indiceMetaMaxima() == null) {
            return null;
        }

        return new Meta(desempenho.indiceMeta(), desempenho.indiceMetaMaxima());
    }

    private AnimalComparado comparar(Animal animal, double indice, Meta meta) {
        return new AnimalComparado(animal, indice, indicadorDesempenho.calcularPontuacaoRanking(indice, meta));
    }

    private List<AnimalComparado> montarDemaisAnimais(Long animalId, Long loteId, JanelaComparacao janela, Meta meta) {
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
                .filter(pesagens -> pesagens.size() >= RequisitosAnalise.MINIMO_PESAGENS)
                .filter(pesagens -> cobreJanela(pesagens, janela))
                .map(pesagens -> {
                    Double indice = indicadorDesempenho.calcularHistorico(
                            pesagens.get(0),
                            pesagens.get(pesagens.size() - 1)
                    );
                    return indice == null ? null : comparar(pesagens.get(0).getAnimal(), indice, meta);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(AnimalComparado::pontuacao).reversed())
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
            DesempenhoContextoDTO desempenho,
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
                desempenho.indiceMeta(),
                desempenho.indiceMetaMaxima(),
                calculadoraEstatisticasLote.calcularMedia(pesos),
                calcularCustoMedio(comparados, usuarioLogado)
        );
    }

    /** Posição e percentil seguem a pontuação da aplicação; diferenças e percentuais seguem o índice. */
    private PosicaoAnimalLoteDTO montarPosicao(
            AnimalComparado animalAnalisado,
            List<AnimalComparado> demaisAnimais,
            EstatisticasLoteDTO estatisticas) {

        List<Double> pontuacoesDemaisAnimais = demaisAnimais.stream()
                .map(AnimalComparado::pontuacao)
                .toList();

        double indiceAnimal = animalAnalisado.indice();
        Double indiceMedio = estatisticas.indiceMedio();

        return new PosicaoAnimalLoteDTO(
                calculadoraEstatisticasLote.calcularPosicao(
                        animalAnalisado.pontuacao(),
                        pontuacoesDemaisAnimais
                ),
                estatisticas.animaisComparados(),
                calculadoraEstatisticasLote.calcularPercentil(
                        animalAnalisado.pontuacao(),
                        pontuacoesDemaisAnimais
                ),
                indiceAnimal - indiceMedio,
                calculadoraEstatisticasLote.calcularPercentualDaReferencia(
                        indiceAnimal,
                        indiceMedio
                ),
                animalAnalisado.animal().getPesoAtual() - estatisticas.pesoMedioKg()
        );
    }

    private Double calcularCustoMedio(
            List<AnimalComparado> comparados,
            Usuario usuarioLogado) {

        if (!politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuarioLogado)) {
            return null;
        }

        List<Animal> animais = comparados.stream()
                .map(AnimalComparado::animal)
                .toList();

        return rateioCustoService.calcularCustoMedio(animais);
    }
}