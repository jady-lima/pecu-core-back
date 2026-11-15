package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.model.Vacinacao;
import com.pecucore.system.core.repository.VacinacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AlertaSanitarioService {

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    @Autowired
    private AlertaService alertaService;

    @Autowired
    private RegraAlertaService regraAlertaService;

    public void verificarVacinaProximaVencimento(Animal animal) {
        Optional<Double> prazo = regraAlertaService.getParametroAtivo(animal, TipoAlerta.VACINA_PROX_VENC);

        if (prazo.isEmpty()) {
            return;
        }

        LocalDate hoje = LocalDate.now();

        vacinacoes(animal).stream()
                .map(Vacinacao::getDataProximaDose)
                .filter(Objects::nonNull)
                .filter(data -> {
                    long dias = ChronoUnit.DAYS.between(hoje, data);
                    return dias >= 0 && dias <= prazo.get();
                })
                .findFirst()
                .ifPresent(data -> alertaService.createAlertaSeNaoExistir(animal, TipoAlerta.VACINA_PROX_VENC, "Atenção: vacinação próxima. A próxima dose da vacina está prevista para " + data));
    }

    public void verificarVacinaVencida(Animal animal) {
        LocalDate hoje = LocalDate.now();

        vacinacoes(animal).stream()
                .map(Vacinacao::getDataProximaDose)
                .filter(Objects::nonNull)
                .filter(data -> data.isBefore(hoje))
                .findFirst()
                .ifPresent(data -> alertaService.createAlertaSeNaoExistir(animal, TipoAlerta.VACINA_VENC, "A vacina do animal está vencida. A próxima dose estava prevista para " + data));
    }

    public void verificarPeriodoCarencia(Animal animal) {
        LocalDate hoje = LocalDate.now();

        vacinacoes(animal).stream()
                .map(Vacinacao::getDataFimCarencia)
                .filter(Objects::nonNull)
                .filter(data -> data.isAfter(hoje))
                .findFirst()
                .ifPresent(data -> alertaService.createAlertaSeNaoExistir(animal, TipoAlerta.PERIODO_CARENCIA, "Animal ainda está em período de carência. Animal estará em período de carência até " + data));
    }

    private List<Vacinacao> vacinacoes(Animal animal) {
        return vacinacaoRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());
    }
}
