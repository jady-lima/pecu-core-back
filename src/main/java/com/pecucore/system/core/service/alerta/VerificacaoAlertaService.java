package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.service.AnimalService;
import com.pecucore.system.core.service.alerta.verificador.GatilhoAlerta;
import com.pecucore.system.core.service.alerta.verificador.VerificadorAlerta;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerificacaoAlertaService {

    @Autowired
    private List<VerificadorAlerta> verificadores;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private AlertaService alertaService;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void verificar(Long animalId, GatilhoAlerta gatilho) {
        Animal animal = animalService.getAnimalById(animalId);

        verificadores.stream()
                .filter(verificador -> verificador.getGatilhos().contains(gatilho))
                .forEach(verificador -> verificador.avaliar(animal).ifPresentOrElse(
                        ocorrencia -> alertaService.createAlertaSeNaoExistir(animal, verificador.getTipo(), ocorrencia),
                        () -> alertaService.resolverAlertasAutomaticamente(animal, verificador.getTipo())
                ));
    }
}
