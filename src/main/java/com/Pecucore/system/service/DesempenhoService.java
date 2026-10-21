package com.Pecucore.system.service;

import com.Pecucore.system.dto.ComparacaoDesempenhoResponseDTO;
import com.Pecucore.system.dto.CurvaCrescimentoResponseDTO;
import com.Pecucore.system.exception.ConflitoNegocioException;
import com.Pecucore.system.exception.RecursoNaoEncontradoException;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.service.calculo.GmdService;
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
    private GmdService gmdService;

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

        Double gmdAtual = gmdService.calculateGmdHistorico(
                primeiraPesagem,
                ultimaPesagem
        );

        if (gmdAtual == null) {
            throw new ConflitoNegocioException(
                    "Não foi possível calcular o GMD do animal. As pesagens precisam ter datas diferentes."
            );
        }

        double gmdEsperado = metaDesempenhoService.getGmdEsperado(animal.getLote())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Meta de desempenho não encontrada para o lote nem para a fase."
                ));

        double diferenca = gmdAtual - gmdEsperado;

        double percentualDaMeta =
                gmdService.calcularPercentualDaMeta(gmdAtual, gmdEsperado);

        double percentualAbaixoDaMeta =
                100 - percentualDaMeta;

        boolean abaixoDaMeta =
                gmdAtual < gmdEsperado;

        return new ComparacaoDesempenhoResponseDTO(
                animal.getBrinco(),
                animal.getLote().getFinalidade(),
                gmdAtual,
                gmdEsperado,
                diferenca,
                percentualDaMeta,
                percentualAbaixoDaMeta,
                abaixoDaMeta
        );
    }
}