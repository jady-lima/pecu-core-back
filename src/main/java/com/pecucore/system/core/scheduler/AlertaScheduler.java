package com.pecucore.system.core.scheduler;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.repository.AnimalRepository;
import com.pecucore.system.core.service.alerta.VerificacaoAlertaService;
import com.pecucore.system.core.service.alerta.verificador.GatilhoAlerta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertaScheduler {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private VerificacaoAlertaService verificacaoAlertaService;

    @Scheduled(cron = "0 0 7 * * *")
    public void verificarAlertas() {
        for (Animal animal : animalRepository.findAll()) {
            verificacaoAlertaService.verificar(animal, GatilhoAlerta.ROTINA_DIARIA);
        }
    }
}
