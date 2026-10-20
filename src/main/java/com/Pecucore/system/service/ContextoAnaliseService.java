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

    @Autowired
    private AnimalService animalService;

    @Autowired
    private GmdService gmdService;

    @Autowired
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira;

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

    @Autowired
    private CarenciaService carenciaService;

    @Transactional(readOnly = true)
    public ContextoAnaliseAnimalDTO montarIndividual(Long animalId, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalById(animalId, usuarioLogado);

        return montarContextoAnimal(animal, usuarioLogado);
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
        boolean emCarencia = carenciaService.estaEmCarencia(animalId);

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


}
