package com.pecucore.system.core.service.alerta.verificador;

import com.pecucore.system.core.extensao.alerta.VerificadorAlerta;
import com.pecucore.system.core.extensao.alerta.GatilhoAlerta;
import com.pecucore.system.core.extensao.alerta.OcorrenciaAlerta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.VacinacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Component
public class VerificadorVacinaVencida implements VerificadorAlerta {

    @Autowired
    private VacinacaoService vacinacaoService;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.VACINA_VENC;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.VACINACAO_REGISTRADA, GatilhoAlerta.ROTINA_DIARIA);
    }

    @Override
    public Optional<OcorrenciaAlerta> avaliar(Animal animal) {
        LocalDate hoje = LocalDate.now();

        return vacinacaoService.getUltimasVacinacoesByAnimalId(animal.getId()).stream()
                .filter(vacinacao -> vacinacao.getDataProximaDose() != null)
                .filter(vacinacao -> vacinacao.getDataProximaDose().isBefore(hoje))
                .findFirst()
                .map(vacinacao -> new OcorrenciaAlerta("vacinacao:" + vacinacao.getId(),
                        "Vacina vencida: a próxima dose da vacina " + vacinacao.getVacina().getNome()
                                + " estava prevista para " + FormatadorAlerta.data(vacinacao.getDataProximaDose()) + "."));
    }
}
