package com.pecucore.system.core.service.lote;

import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.Propriedade;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.animal.AnimalConsultaService;
import com.pecucore.system.core.service.lote.SugestaoReagrupamento.LoteDestino;
import com.pecucore.system.core.service.propriedade.PropriedadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReagrupamentoService {

    @Autowired
    private PropriedadeService propriedadeService;

    @Autowired
    private LoteService loteService;

    @Autowired
    private AnimalConsultaService animalConsultaService;

    @Autowired
    private PoliticaAgrupamento politicaAgrupamento;

    public List<SugestaoReagrupamento> getSugestoesReagrupamento(Long propriedadeId, Usuario usuarioLogado) {
        Propriedade propriedade = propriedadeService.getPropriedadeById(propriedadeId, usuarioLogado);

        List<Lote> lotes = loteService.getLotesByPropriedadeId(propriedade.getId());
        Map<Long, Long> vagasPorLote = new HashMap<>();

        return animalConsultaService.getAnimaisAtivosByPropriedadeId(propriedade.getId()).stream()
            .map(animal -> montarSugestao(animal, lotes, vagasPorLote))
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<SugestaoReagrupamento> montarSugestao(Animal animal, List<Lote> lotes, Map<Long, Long> vagasPorLote) {
        return politicaAgrupamento.sugerirFase(animal)
            .filter(faseSugerida -> !faseSugerida.equals(animal.getLote().getFinalidade()))
            .map(faseSugerida -> new SugestaoReagrupamento(
                    animal,
                    faseSugerida,
                    montarLotesDestino(faseSugerida, lotes, vagasPorLote)
            ));
    }

    private List<LoteDestino> montarLotesDestino(String fase, List<Lote> lotes, Map<Long, Long> vagasPorLote) {
        return lotes.stream()
            .filter(lote -> fase.equals(lote.getFinalidade()))
            .map(lote -> new LoteDestino(lote, vagasPorLote.computeIfAbsent(lote.getId(), id -> calcularVagas(lote))))
            .filter(destino -> destino.vagas() > 0)
            .toList();
    }

    private long calcularVagas(Lote lote) {
        return Math.max(0, lote.getCapacidade() - animalConsultaService.countAnimaisAtivosByLoteId(lote.getId()));
    }
}
