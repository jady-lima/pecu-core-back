package com.pecucore.system.core.service.desempenho;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.extensao.desempenho.Meta;
import com.pecucore.system.core.extensao.desempenho.ResultadoMeta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.animal.AnimalService;
import com.pecucore.system.core.service.lote.LoteService;
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
    private LoteService loteService;

    public List<Pesagem> getCurvaCrescimento(int brinco, Usuario usuarioLogado) {
        return pesagemService.getHistorico(brinco, usuarioLogado);
    }

    public ResultadoComparacaoDesempenho comparar(int brinco, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalByBrinco(brinco, usuarioLogado);

        List<Pesagem> pesagens = pesagemService.getHistorico(brinco, usuarioLogado);

        if (pesagens.size() < 2) {
            throw new ConflitoNegocioException(
                    "O animal precisa possuir pelo menos duas pesagens para avaliação de desempenho."
            );
        }

        Double indiceAtual = indicadorDesempenho.calcularHistorico(
                pesagens.get(0),
                pesagens.get(pesagens.size() - 1)
        );

        if (indiceAtual == null) {
            throw new ConflitoNegocioException(
                    "Não foi possível calcular o " + indicadorDesempenho.getNome()
                            + " do animal. As pesagens precisam ter datas diferentes."
            );
        }

        Meta meta = loteService.getMetaDesempenho(animal.getLote())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Meta de desempenho não encontrada para o lote nem para a fase."
                ));

        ResultadoMeta resultadoMeta = meta.avaliar(indiceAtual);
        Double minimo = meta.minimo();

        // Percentual só faz sentido sobre um mínimo positivo; senão fica nulo (antes: NullPointerException).
        Double percentualDaMeta =
                calculadoraEstatisticasLote.calcularPercentualDaReferencia(indiceAtual, minimo);

        return new ResultadoComparacaoDesempenho(
                animal,
                indicadorDesempenho.getNome(),
                indicadorDesempenho.getUnidade(),
                indiceAtual,
                minimo,
                meta.maximo(),
                minimo != null ? indiceAtual - minimo : null,
                percentualDaMeta,
                percentualDaMeta != null ? 100 - percentualDaMeta : null,
                resultadoMeta == ResultadoMeta.ABAIXO,
                resultadoMeta
        );
    }

}
