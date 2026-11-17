package com.pecucore.system.core.service.analise;

import com.pecucore.system.core.dto.analise.ContextoAnaliseLoteDTO;
import com.pecucore.system.core.dto.analise.DadosLoteDTO;
import com.pecucore.system.core.dto.analise.DesempenhoLoteDTO;
import com.pecucore.system.core.dto.analise.SanidadeLoteDTO;
import com.pecucore.system.core.exception.DadosInsuficientesException;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.alerta.AlertaService;
import com.pecucore.system.core.service.animal.AnimalConsultaService;
import com.pecucore.system.core.service.calculo.CalculadoraEstatisticasLote;
import com.pecucore.system.core.service.custo.RateioCustoService;
import com.pecucore.system.core.service.lote.LoteService;
import com.pecucore.system.core.service.lote.ReagrupamentoService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import com.pecucore.system.core.service.politica.PoliticaVisibilidadeFinanceira;
import com.pecucore.system.core.service.sanidade.CarenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ContextoAnaliseLoteService {

    private static final int MINIMO_ANIMAIS_AVALIADOS = 2;

    private record DesempenhoAnimal(Double indice, Double escoreCorporal) {
    }

    @Autowired
    private AnimalConsultaService animalConsultaService;

    @Autowired
    private PesagemService pesagemService;

    @Autowired
    private LoteService loteService;

    @Autowired
    private ReagrupamentoService reagrupamentoService;

    @Autowired
    private CarenciaService carenciaService;

    @Autowired
    private AlertaService alertaService;

    @Autowired
    private RateioCustoService rateioCustoService;

    @Autowired
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira;

    @Autowired
    private IndicadorDesempenho indicadorDesempenho;

    @Autowired
    private CalculadoraEstatisticasLote calculadoraEstatisticasLote;

    @Transactional(readOnly = true)
    public ContextoAnaliseLoteDTO montarLote(Lote lote, Usuario usuarioLogado) {
        LocalDate hoje = LocalDate.now();
        List<Animal> animais = animalConsultaService.getAnimaisAtivosByLoteId(lote.getId());
        List<DesempenhoAnimal> desempenhos = montarDesempenhosAnimais(lote.getId());

        if (desempenhos.size() < MINIMO_ANIMAIS_AVALIADOS) {
            throw new DadosInsuficientesException(
                    "O lote precisa possuir pelo menos " + MINIMO_ANIMAIS_AVALIADOS
                            + " animais ativos com " + RequisitosAnalise.MINIMO_PESAGENS
                            + " pesagens ou mais para gerar a análise do lote."
            );
        }

        return new ContextoAnaliseLoteDTO(
                hoje,
                montarDadosLote(lote, animais, hoje),
                montarDesempenhoLote(lote, animais, desempenhos),
                montarSanidade(lote.getId(), animais, usuarioLogado),
                montarCustoMedio(animais, usuarioLogado)
        );
    }

    private List<DesempenhoAnimal> montarDesempenhosAnimais(Long loteId) {
        Collection<List<Pesagem>> pesagensPorAnimal = pesagemService.getPesagensAnimaisAtivosByLoteId(loteId).stream()
                .collect(Collectors.groupingBy(pesagem -> pesagem.getAnimal().getId()))
                .values();

        return pesagensPorAnimal.stream()
                .filter(pesagens -> pesagens.size() >= RequisitosAnalise.MINIMO_PESAGENS)
                .map(pesagens -> new DesempenhoAnimal(
                        indicadorDesempenho.calcularHistorico(pesagens.get(0), pesagens.get(pesagens.size() - 1)),
                        getUltimoEscoreCorporal(pesagens).orElse(null)
                ))
                .filter(desempenho -> desempenho.indice() != null)
                .toList();
    }

    private Optional<Double> getUltimoEscoreCorporal(List<Pesagem> pesagens) {
        return pesagens.reversed().stream()
                .map(Pesagem::getEscoreCorporal)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private DadosLoteDTO montarDadosLote(Lote lote, List<Animal> animais, LocalDate hoje) {
        List<Double> idadesMeses = animais.stream()
                .map(animal -> animal.getIdadeMeses(hoje))
                .flatMap(Optional::stream)
                .map(Long::doubleValue)
                .toList();

        Map<String, Long> animaisPorSexo = animais.stream()
                .map(Animal::getSexo)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(sexo -> sexo, TreeMap::new, Collectors.counting()));

        return new DadosLoteDTO(
                lote.getFinalidade(),
                lote.getCapacidade(),
                animais.size(),
                idadesMeses.isEmpty() ? null : calculadoraEstatisticasLote.calcularMedia(idadesMeses),
                animaisPorSexo,
                reagrupamentoService.countAnimaisForaDaFase(animais)
        );
    }

    private DesempenhoLoteDTO montarDesempenhoLote(Lote lote, List<Animal> animais, List<DesempenhoAnimal> desempenhos) {
        List<Double> indices = desempenhos.stream().map(DesempenhoAnimal::indice).toList();
        List<Double> escores = desempenhos.stream().map(DesempenhoAnimal::escoreCorporal).filter(Objects::nonNull).toList();
        List<Double> pesos = animais.stream().map(Animal::getPesoAtual).toList();

        double indiceMedio = calculadoraEstatisticasLote.calcularMedia(indices);
        Double indiceMeta = loteService.getMetaDesempenho(lote).orElse(null);

        return new DesempenhoLoteDTO(
                indicadorDesempenho.getNome(),
                indicadorDesempenho.getUnidade(),
                desempenhos.size(),
                indiceMedio,
                calculadoraEstatisticasLote.calcularMediana(indices),
                Collections.min(indices),
                Collections.max(indices),
                indiceMeta,
                calculadoraEstatisticasLote.calcularPercentualDaReferencia(indiceMedio, indiceMeta),
                indiceMeta != null ? indices.stream().filter(indice -> indice < indiceMeta).count() : null,
                calculadoraEstatisticasLote.calcularMedia(pesos),
                escores.isEmpty() ? null : calculadoraEstatisticasLote.calcularMedia(escores),
                escores.size()
        );
    }

    private SanidadeLoteDTO montarSanidade(Long loteId, List<Animal> animais, Usuario usuarioLogado) {
        long animaisEmCarencia = animais.stream()
                .filter(animal -> carenciaService.estaEmCarencia(animal.getId()))
                .count();

        Map<String, Long> alertasAtivosPorTipo = alertaService.getAlertasAtivosByLoteId(loteId, usuarioLogado).stream()
                .collect(Collectors.groupingBy(alerta -> alerta.getTipo().getCodigo(), TreeMap::new, Collectors.counting()));

        return new SanidadeLoteDTO(animaisEmCarencia, alertasAtivosPorTipo);
    }

    private Double montarCustoMedio(List<Animal> animais, Usuario usuarioLogado) {
        if (!politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuarioLogado)) {
            return null;
        }

        return rateioCustoService.calcularCustoMedio(animais);
    }
}
