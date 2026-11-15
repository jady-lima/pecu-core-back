package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.model.Vacinacao;
import com.pecucore.system.core.service.VacinacaoService;
import com.pecucore.system.core.service.alerta.AlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Component
public class VerificadorPeriodoCarencia implements VerificadorAlerta {

    @Autowired
    private VacinacaoService vacinacaoService;

    @Autowired
    private AlertaService alertaService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.PERIODO_CARENCIA;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.VACINACAO_REGISTRADA, GatilhoAlerta.ROTINA_DIARIA);
    }

    @Override
    public void verificar(Animal animal) {
        LocalDate hoje = LocalDate.now();

        vacinacaoService.getHistoricoByAnimalId(animal.getId()).stream()
                .map(Vacinacao::getDataFimCarencia)
                .filter(Objects::nonNull)
                .filter(data -> data.isAfter(hoje))
                .max(LocalDate::compareTo)
                .ifPresent(data -> alertaService.createAlertaSeNaoExistir(animal, getTipo(),
                        "Animal ainda está em período de carência. Animal estará em período de carência até "
                                + data));
    }
}
