package com.pecucore.system.core.service;

import com.pecucore.system.bovinos.agrupamento.MetaDesempenhoService;
import com.pecucore.system.core.dto.desempenho.ComparacaoDesempenhoResponseDTO;
import com.pecucore.system.core.dto.desempenho.CurvaCrescimentoResponseDTO;
import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.service.calculo.CalculadoraEstatisticasLote;
import com.pecucore.system.core.service.pesagem.PesagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DesempenhoService {

    @Autowired
    private AnimalService animalService;

    @Autowired
    private PesagemService pesagemService;

    @Autowired
    private IndicadorDesempenho indicadorDesempenho;

    @Autowired
    private CalculadoraEstatisticasLote calculadoraEstatisticasLote;

    @Autowired
    private MetaDesempenhoService metaDesempenhoService;

    public List<CurvaCrescimentoResponseDTO> getCurvaCrescimento(int brinco, Usuario usuarioLogado) {

        List<Pesagem> pesagens = pesagemService.getHistorico(brinco, usuarioLogado);

        return pesagens.stream()
                .map(CurvaCrescimentoResponseDTO::new)
                .toList();
    }

    public ComparacaoDesempenhoResponseDTO comparar(int brinco, Usuario usuarioLogado) {

        Animal animal = animalService.getAnimalByBrinco(brinco, usuarioLogado);

        List<Pesagem> pesagens = pesagemService.getHistorico(brinco, usuarioLogado);

        if (pesagens.size() < 2) {
            throw new ConflitoNegocioException(
                "O animal precisa possuir pelo menos duas pesagens para avaliação de desempenho."
            );
        }

        Pesagem primeiraPesagem = pesagens.get(0);
        Pesagem ultimaPesagem = pesagens.get(pesagens.size() - 1);

        Double indiceAtual = indicadorDesempenho.calcularHistorico(
                primeiraPesagem,
                ultimaPesagem
        );

        if (indiceAtual == null) {
            throw new ConflitoNegocioException(
                "Não foi possível calcular o " + indicadorDesempenho.getNome() + " do animal. As pesagens precisam ter datas diferentes."
            );
        }

        double indiceEsperado = metaDesempenhoService.getGmdEsperado(animal.getLote()).orElseThrow(() -> new RecursoNaoEncontradoException("Meta de desempenho não encontrada para o lote nem para a fase."));

        double diferenca = indiceAtual - indiceEsperado;

        double percentualDaMeta = calculadoraEstatisticasLote.calcularPercentualDaReferencia(indiceAtual, indiceEsperado);

        double percentualAbaixoDaMeta = 100 - percentualDaMeta;

        boolean abaixoDaMeta = indiceAtual < indiceEsperado;

        return new ComparacaoDesempenhoResponseDTO(
                animal.getBrinco(),
                animal.getLote().getFinalidade(),
                indicadorDesempenho.getNome(),
                indicadorDesempenho.getUnidade(),
                indiceAtual,
                indiceEsperado,
                diferenca,
                percentualDaMeta,
                percentualAbaixoDaMeta,
                abaixoDaMeta
        );
    }
}