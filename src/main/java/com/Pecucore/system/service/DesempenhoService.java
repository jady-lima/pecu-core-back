package com.Pecucore.system.service;

import com.Pecucore.system.dto.ComparacaoDesempenhoResponseDTO;
import com.Pecucore.system.dto.CurvaCrescimentoResponseDTO;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.MetaDesempenho;
import com.Pecucore.system.model.Pesagem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    public List<CurvaCrescimentoResponseDTO> getCurvaCrescimento(int brinco) {

        List<Pesagem> pesagens = pesagemService.getHistorico(brinco);

        return pesagens.stream()
                .map(CurvaCrescimentoResponseDTO::new)
                .toList();
    }

    public ComparacaoDesempenhoResponseDTO comparar(int brinco) {

        Animal animal = animalService.getAnimalByBrinco(brinco);

        List<Pesagem> pesagens = pesagemService.getHistorico(brinco);

        if (pesagens.size() < 2) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O animal precisa possuir pelo menos duas pesagens para avaliação de desempenho."
            );
        }

        Pesagem primeiraPesagem = pesagens.get(0);
        Pesagem ultimaPesagem = pesagens.get(pesagens.size() - 1);

        Double gmdAtual = gmdService.calculateGmdHistorico(
                primeiraPesagem,
                ultimaPesagem
        );

        MetaDesempenho meta = metaDesempenhoService.getByFase(
                animal.getLote().getFinalidade()
        );

        double diferenca = gmdAtual - meta.getGmdEsperado();

        double percentualDaMeta =
                (gmdAtual / meta.getGmdEsperado()) * 100;

        double percentualAbaixoDaMeta =
                100 - percentualDaMeta;

        boolean abaixoDaMeta =
                gmdAtual < meta.getGmdEsperado();

        return new ComparacaoDesempenhoResponseDTO(
                animal.getBrinco(),
                animal.getLote().getFinalidade(),
                gmdAtual,
                meta.getGmdEsperado(),
                diferenca,
                percentualDaMeta,
                percentualAbaixoDaMeta,
                abaixoDaMeta
        );
    }
}