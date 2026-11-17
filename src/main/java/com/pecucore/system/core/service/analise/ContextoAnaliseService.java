package com.pecucore.system.core.service.analise;

import com.pecucore.system.bovinos.service.sanidade.CarenciaService;
import com.pecucore.system.core.dto.analise.AlertaContextoDTO;
import com.pecucore.system.core.dto.analise.AlimentacaoContextoDTO;
import com.pecucore.system.core.dto.analise.ContextoAnaliseAnimalDTO;
import com.pecucore.system.core.dto.analise.DadosAnimalDTO;
import com.pecucore.system.core.dto.analise.DesempenhoContextoDTO;
import com.pecucore.system.core.dto.analise.MedicamentoContextoDTO;
import com.pecucore.system.core.dto.analise.PesagemContextoDTO;
import com.pecucore.system.core.dto.analise.ResumoCustoDTO;
import com.pecucore.system.core.dto.analise.VacinacaoContextoDTO;
import com.pecucore.system.core.exception.DadosInsuficientesException;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Medicamento;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.model.Vacinacao;
import com.pecucore.system.core.service.*;
import com.pecucore.system.core.service.alerta.AlertaService;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.service.calculo.CalculadoraEstatisticasLote;
import com.pecucore.system.core.service.custo.CustoService;
import com.pecucore.system.core.service.pesagem.PesagemService;
import com.pecucore.system.core.service.politica.PoliticaVisibilidadeFinanceira;
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
    private IndicadorDesempenho indicadorDesempenho;

    @Autowired
    private CalculadoraEstatisticasLote calculadoraEstatisticasLote;

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
    private AlertaService alertaService;

    @Autowired
    private CustoService custoService;

    @Autowired
    private LoteService loteService;

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

        Double indicePeriodo = indicadorDesempenho.calcularHistorico(primeiraPesagem, ultimaPesagem);
        long diasAvaliados = ChronoUnit.DAYS.between(primeiraPesagem.getData(), ultimaPesagem.getData());

        Double indiceMeta = loteService.getMetaDesempenho(animal.getLote()).orElse(null);

        Double percentualDaMeta = calculadoraEstatisticasLote.calcularPercentualDaReferencia(indicePeriodo, indiceMeta);

        return new DesempenhoContextoDTO(
                indicadorDesempenho.getNome(),
                indicadorDesempenho.getUnidade(),
                indicePeriodo,
                diasAvaliados,
                indiceMeta,
                percentualDaMeta
        );
    }

    private List<AlertaContextoDTO> montarAlertas(Long animalId, Usuario usuarioLogado) {
        return alertaService.getHistoricoByAnimalId(animalId, usuarioLogado).stream()
                    .map(AlertaContextoDTO::new)
                    .toList();
    }

    private ResumoCustoDTO montarCustos(Long animalId, Usuario usuarioLogado) {
        if (!politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuarioLogado)) {
            return null;
        }

        return new ResumoCustoDTO(custoService.getCustosByAnimalId(animalId));
    }


}
