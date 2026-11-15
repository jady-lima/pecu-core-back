package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.VacinacaoService;
import com.pecucore.system.core.service.alerta.AlertaService;
import com.pecucore.system.core.service.alerta.RegraAlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

@Component
public class VerificadorVacinaProximaVencimento implements VerificadorAlerta {

    @Autowired
    private VacinacaoService vacinacaoService;

    @Autowired
    private RegraAlertaService regraAlertaService;

    @Autowired
    private AlertaService alertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.VACINA_PROX_VENC;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.VACINACAO_REGISTRADA, GatilhoAlerta.ROTINA_DIARIA);
    }

    @Override
    public void verificar(Animal animal) {
        Optional<Double> prazo = regraAlertaService.getParametroAtivo(animal, getTipo());

        if (prazo.isEmpty()) {
            return;
        }

        LocalDate hoje = LocalDate.now();

        vacinacaoService.getUltimasVacinacoesByAnimalId(animal.getId()).stream()
                .filter(vacinacao -> vacinacao.getDataProximaDose() != null)
                .filter(vacinacao -> {
                    long dias = ChronoUnit.DAYS.between(hoje, vacinacao.getDataProximaDose());
                    return dias >= 0 && dias <= prazo.get();
                })
                .findFirst()
                .ifPresent(vacinacao -> alertaService.createAlertaSeNaoExistir(animal, getTipo(),
                        "Atenção: vacinação próxima. A próxima dose da vacina "
                                + vacinacao.getVacina().getNome() + " está prevista para "
                                + vacinacao.getDataProximaDose()));
    }
}
