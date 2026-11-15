package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.VacinacaoService;
import com.pecucore.system.core.service.alerta.AlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
public class VerificadorVacinaVencida implements VerificadorAlerta {

    @Autowired
    private VacinacaoService vacinacaoService;

    @Autowired
    private AlertaService alertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.VACINA_VENC;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.VACINACAO_REGISTRADA, GatilhoAlerta.ROTINA_DIARIA);
    }

    @Override
    public void verificar(Animal animal) {
        LocalDate hoje = LocalDate.now();

        vacinacaoService.getUltimasVacinacoesByAnimalId(animal.getId()).stream()
                .filter(vacinacao -> vacinacao.getDataProximaDose() != null)
                .filter(vacinacao -> vacinacao.getDataProximaDose().isBefore(hoje))
                .findFirst()
                .ifPresent(vacinacao -> alertaService.createAlertaSeNaoExistir(animal, getTipo(),
                        "A vacina " + vacinacao.getVacina().getNome()
                                + " do animal está vencida. A próxima dose estava prevista para "
                                + vacinacao.getDataProximaDose()));
    }
}
