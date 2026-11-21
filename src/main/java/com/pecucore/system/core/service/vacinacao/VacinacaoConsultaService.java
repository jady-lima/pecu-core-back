package com.pecucore.system.core.service.vacinacao;

import com.pecucore.system.core.model.Vacinacao;
import com.pecucore.system.core.repository.VacinacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VacinacaoConsultaService {

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    public boolean existsVacinacaoByVacinaId(Long vacinaId) {
        return vacinacaoRepository.existsByVacinaId(vacinaId);
    }

    public Optional<LocalDate> getDataFimCarenciaVigente(Long animalId, LocalDate dataReferencia) {
        return vacinacaoRepository
                .findFirstByAnimalIdAndDataFimCarenciaAfterOrderByDataFimCarenciaDesc(animalId, dataReferencia)
                .map(Vacinacao::getDataFimCarencia);
    }

    /**
     * Última vacinação de cada vacina aplicada no animal: é ela que define a próxima dose em vigor.
     * Fica neste serviço (só repositório) para ser usada pelos verificadores de alerta e pelas
     * políticas das aplicações sem criar dependência circular com o VacinacaoService.
     */
    public List<Vacinacao> getUltimasVacinacoesByAnimalId(Long animalId) {
        Map<Long, Vacinacao> ultimaPorVacina = new LinkedHashMap<>();

        vacinacaoRepository.findByAnimalIdOrderByDataAscIdAsc(animalId)
                .forEach(vacinacao -> ultimaPorVacina.put(vacinacao.getVacina().getId(), vacinacao));

        return List.copyOf(ultimaPorVacina.values());
    }
}