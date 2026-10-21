package com.Pecucore.system.service;

import com.Pecucore.system.dto.analise.AlertaContextoDTO;
import com.Pecucore.system.dto.analise.AlimentacaoContextoDTO;
import com.Pecucore.system.dto.analise.ContextoAnaliseAnimalDTO;
import com.Pecucore.system.dto.analise.DadosAnimalDTO;
import com.Pecucore.system.dto.analise.DesempenhoContextoDTO;
import com.Pecucore.system.dto.analise.MedicamentoContextoDTO;
import com.Pecucore.system.dto.analise.PesagemContextoDTO;
import com.Pecucore.system.dto.analise.ResumoCustoDTO;
import com.Pecucore.system.dto.analise.VacinacaoContextoDTO;
import com.Pecucore.system.exception.DadosInsuficientesException;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Medicamento;
import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.model.Vacinacao;
import com.Pecucore.system.repository.AlertaRepository;
import com.Pecucore.system.service.calculo.GmdService;
import com.Pecucore.system.service.politica.PoliticaVisibilidadeFinanceira;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
    private PesagemService pesagemService;

    @Autowired
    private AlimentacaoService alimentacaoService;

    @Autowired
    private VacinacaoService vacinacaoService;

    @Autowired
    private MedicamentoService medicamentoService;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private CustoService custoService;

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

        List<Pesagem> pesagens = pesagemService.getHistoricoByAnimalId(animalId);

        if (pesagens.size() < MINIMO_PESAGENS) {
            throw new DadosInsuficientesException(
                "O animal precisa possuir pelo menos " + MINIMO_PESAGENS + " pesagens para gerar a análise."
            );
        }

        List<Vacinacao> vacinacoes = vacinacaoService.getHistoricoByAnimalId(animalId);
        List<Medicamento> medicamentos = medicamentoService.getHistoricoByAnimalId(animalId);
        boolean emCarencia = carenciaService.estaEmCarencia(animalId);

        return new ContextoAnaliseAnimalDTO(
            hoje,
            new DadosAnimalDTO(animal, emCarencia, hoje),
            montarDesempenho(animal, pesagens),
            pesagens.stream().map(PesagemContextoDTO::new).toList(),
            alimentacaoService.getHistoricoByAnimalId(animalId).stream().map(AlimentacaoContextoDTO::new).toList(),
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

        return new ResumoCustoDTO(custoService.getByAnimalId(animalId));
    }


}
