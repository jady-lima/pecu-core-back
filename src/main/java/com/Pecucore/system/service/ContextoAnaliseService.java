package com.Pecucore.system.service;

import com.Pecucore.system.dto.analise.AlertaContextoDTO;
import com.Pecucore.system.dto.analise.AlimentacaoContextoDTO;
import com.Pecucore.system.dto.analise.ContextoAnaliseAnimalDTO;
import com.Pecucore.system.dto.analise.ContextoAnaliseComparativaDTO;
import com.Pecucore.system.dto.analise.DadosAnimalDTO;
import com.Pecucore.system.dto.analise.DesempenhoContextoDTO;
import com.Pecucore.system.dto.analise.EstatisticasLoteDTO;
import com.Pecucore.system.dto.analise.MedicamentoContextoDTO;
import com.Pecucore.system.dto.analise.ParLoteDTO;
import com.Pecucore.system.dto.analise.PesagemContextoDTO;
import com.Pecucore.system.dto.analise.PosicaoAnimalLoteDTO;
import com.Pecucore.system.dto.analise.ResumoCustoDTO;
import com.Pecucore.system.dto.analise.VacinacaoContextoDTO;
import com.Pecucore.system.exception.DadosInsuficientesException;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Custo;
import com.Pecucore.system.model.Medicamento;
import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.model.StatusAnimal;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.model.Vacinacao;
import com.Pecucore.system.repository.AlertaRepository;
import com.Pecucore.system.repository.AlimentacaoRepository;
import com.Pecucore.system.repository.AnimalRepository;
import com.Pecucore.system.repository.CustoRepository;
import com.Pecucore.system.repository.MedicamentoRepository;
import com.Pecucore.system.repository.PesagemRepository;
import com.Pecucore.system.repository.VacinacaoRepository;
import com.Pecucore.system.service.calculo.CalculadoraEstatisticasLote;
import com.Pecucore.system.service.calculo.GmdService;
import com.Pecucore.system.service.politica.PoliticaVisibilidadeFinanceira;
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
public class ContextoAnaliseService {

    private static final int MINIMO_PESAGENS = 2;
    private static final int MINIMO_ANIMAIS_COMPARACAO = 3;

    private record AnimalComparado(Animal animal, Double gmd) {
    }

    @Autowired
    private AnimalService animalService;

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
    private AlimentacaoRepository alimentacaoRepository;

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private CustoRepository custoRepository;

    @Autowired
    private MetaDesempenhoService metaDesempenhoService;

    @Transactional(readOnly = true)
    public ContextoAnaliseAnimalDTO montarIndividual(Long animalId, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalById(animalId, usuarioLogado);

        return montarContextoAnimal(animal, usuarioLogado);
    }

    @Transactional(readOnly = true)
    public ContextoAnaliseComparativaDTO montarComparativo(Long animalId, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalById(animalId, usuarioLogado);

        if (animal.getLote() == null) {
            throw new DadosInsuficientesException("O animal precisa estar vinculado a um lote para gerar a análise comparativa.");
        }

        ContextoAnaliseAnimalDTO contextoAnimal = montarContextoAnimal(animal, usuarioLogado);
        Double gmdAnimal = contextoAnimal.desempenho().gmdPeriodoKgDia();

        if (gmdAnimal == null) {
            throw new DadosInsuficientesException("Não foi possível calcular o GMD do animal. As pesagens precisam ter datas diferentes.");
        }

        Long loteId = animal.getLote().getId();
        List<AnimalComparado> demaisAnimais = montarDemaisAnimais(animalId, loteId);

        if (demaisAnimais.size() + 1 < MINIMO_ANIMAIS_COMPARACAO) {
            throw new DadosInsuficientesException("O lote precisa possuir pelo menos " + MINIMO_ANIMAIS_COMPARACAO + " animais ativos com " + MINIMO_PESAGENS + " pesagens ou mais para gerar a análise comparativa.");
        }

        List<AnimalComparado> comparados = new ArrayList<>(demaisAnimais);
        comparados.add(new AnimalComparado(animal, gmdAnimal));

        EstatisticasLoteDTO estatisticas = montarEstatisticasLote(loteId, contextoAnimal.desempenho().gmdMetaKgDia(), comparados, usuarioLogado);

        return new ContextoAnaliseComparativaDTO(
            contextoAnimal.dataReferencia(),
            contextoAnimal,
            montarPosicao(animal, gmdAnimal, demaisAnimais, estatisticas),
            estatisticas,
            demaisAnimais.stream().map(comparado -> new ParLoteDTO(comparado.animal(), comparado.gmd(), contextoAnimal.dataReferencia())).toList()
        );
    }

    private ContextoAnaliseAnimalDTO montarContextoAnimal(Animal animal, Usuario usuarioLogado) {
        Long animalId = animal.getId();
        LocalDate hoje = LocalDate.now();

        List<Pesagem> pesagens = pesagemRepository.findByAnimalIdOrderByDataAscIdAsc(animalId);

        if (pesagens.size() < MINIMO_PESAGENS) {
            throw new DadosInsuficientesException(
                "O animal precisa possuir pelo menos " + MINIMO_PESAGENS + " pesagens para gerar a análise."
            );
        }

        List<Vacinacao> vacinacoes = vacinacaoRepository.findByAnimalIdOrderByDataAscIdAsc(animalId);
        List<Medicamento> medicamentos = medicamentoRepository.findByAnimalIdOrderByDataAscIdAsc(animalId);
        boolean emCarencia = estaEmCarencia(vacinacoes, medicamentos, hoje);

        return new ContextoAnaliseAnimalDTO(
            hoje,
            new DadosAnimalDTO(animal, emCarencia, hoje),
            montarDesempenho(animal, pesagens),
            pesagens.stream().map(PesagemContextoDTO::new).toList(),
            alimentacaoRepository.findByAnimalIdOrderByDataAscIdAsc(animalId).stream().map(AlimentacaoContextoDTO::new).toList(),
            vacinacoes.stream().map(VacinacaoContextoDTO::new).toList(),
            medicamentos.stream().map(MedicamentoContextoDTO::new).toList(),
            montarAlertas(animalId, usuarioLogado),
            montarCustos(animalId, usuarioLogado)
        );
    }

    private DesempenhoContextoDTO montarDesempenho(Animal animal, List<Pesagem> pesagens) {
        Pesagem primeiraPesagem = pesagens.get(0);
        Pesagem ultimaPesagem = pesagens.get(pesagens.size() - 1);

        Double gmdPeriodo = gmdService.calculateGmdHistorico(primeiraPesagem, ultimaPesagem);
        long diasAvaliados = ChronoUnit.DAYS.between(primeiraPesagem.getData(), ultimaPesagem.getData());

        Double gmdMeta = metaDesempenhoService.getGmdEsperado(animal.getLote()).orElse(null);

        Double percentualDaMeta = null;
        if (gmdPeriodo != null && gmdMeta != null && gmdMeta > 0) {
            percentualDaMeta = (gmdPeriodo / gmdMeta) * 100;
        }

        return new DesempenhoContextoDTO(gmdPeriodo, diasAvaliados, gmdMeta, percentualDaMeta);
    }

    private boolean estaEmCarencia(List<Vacinacao> vacinacoes, List<Medicamento> medicamentos, LocalDate hoje) {
        boolean carenciaVacina = vacinacoes.stream().anyMatch(vacinacao -> vacinacao.getDataFimCarencia().isAfter(hoje));

        boolean carenciaMedicamento = medicamentos.stream().anyMatch(medicamento -> medicamento.getDataFimCarencia().isAfter(hoje));

        return carenciaVacina || carenciaMedicamento;
    }

    private List<AlertaContextoDTO> montarAlertas(Long animalId, Usuario usuarioLogado) {
        return alertaRepository.findByAnimalIdOrderByDataCriacaoDesc(animalId).stream()
                               .filter(alerta -> politicaVisibilidadeFinanceira.podeVerAlerta(alerta.getTipo(), usuarioLogado))
                               .map(AlertaContextoDTO::new)
                               .toList();
    }

    private ResumoCustoDTO montarCustos(Long animalId, Usuario usuarioLogado) {
        if (!politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuarioLogado)) {
            return null;
        }

        return new ResumoCustoDTO(custoRepository.findByAnimalIdOrderByDataAscIdAsc(animalId));
    }

    private List<AnimalComparado> montarDemaisAnimais(Long animalId, Long loteId) {
        Map<Long, List<Pesagem>> pesagensPorAnimal = pesagemRepository.findByAnimalLoteIdAndAnimalStatusOrderByAnimalIdAscDataAscIdAsc(loteId, StatusAnimal.ATIVO).stream()
            .filter(pesagem -> !pesagem.getAnimal().getId().equals(animalId))
            .collect(Collectors.groupingBy(pesagem -> pesagem.getAnimal().getId(), LinkedHashMap::new, Collectors.toList()));

        return pesagensPorAnimal.values().stream()
                                .filter(pesagens -> pesagens.size() >= MINIMO_PESAGENS)
                                .map(pesagens -> new AnimalComparado(
                                    pesagens.get(0).getAnimal(),
                                    gmdService.calculateGmdHistorico(pesagens.get(0), pesagens.get(pesagens.size() - 1))
                                ))
                                .filter(comparado -> comparado.gmd() != null)
                                .sorted(Comparator.comparing(AnimalComparado::gmd).reversed())
                                .toList();
    }

    private EstatisticasLoteDTO montarEstatisticasLote(Long loteId, Double gmdMeta, List<AnimalComparado> comparados, Usuario usuarioLogado) {
        List<Double> gmds = comparados.stream().map(AnimalComparado::gmd).toList();
        List<Double> pesos = comparados.stream().map(comparado -> comparado.animal().getPesoAtual()).toList();

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

    private PosicaoAnimalLoteDTO montarPosicao(Animal animal, Double gmdAnimal, List<AnimalComparado> demaisAnimais, EstatisticasLoteDTO estatisticas) {
        List<Double> gmdsDemaisAnimais = demaisAnimais.stream().map(AnimalComparado::gmd).toList();
        Double gmdMedio = estatisticas.gmdMedioKgDia();

        return new PosicaoAnimalLoteDTO(
            calculadoraEstatisticasLote.calcularPosicao(gmdAnimal, gmdsDemaisAnimais),
            estatisticas.animaisComparados(),
            calculadoraEstatisticasLote.calcularPercentil(gmdAnimal, gmdsDemaisAnimais),
            gmdAnimal - gmdMedio,
            calculadoraEstatisticasLote.calcularPercentualDaReferencia(gmdAnimal, gmdMedio),
            animal.getPesoAtual() - estatisticas.pesoMedioKg()
        );
    }

    private Double calcularCustoMedio(List<AnimalComparado> comparados, Usuario usuarioLogado) {
        if (!politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuarioLogado)) {
            return null;
        }

        List<Long> animalIds = comparados.stream().map(comparado -> comparado.animal().getId()).toList();
        double total = custoRepository.findByAnimalIdIn(animalIds).stream().mapToDouble(Custo::getValor).sum();

        return total / animalIds.size();
    }
}
