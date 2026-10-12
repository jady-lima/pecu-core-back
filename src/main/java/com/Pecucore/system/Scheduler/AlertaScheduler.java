package com.Pecucore.system.Scheduler;

import com.Pecucore.system.service.AlertaService;
import com.Pecucore.system.repository.AnimalRepository;
import com.Pecucore.system.model.Animal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertaScheduler {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AlertaService alertaService;

    @Scheduled(cron = "0 0 7 * * *")
    public void verificarAlertas() {

        for (Animal animal : animalRepository.findAll()) {
            alertaService.verificarPesagemAtrasada(animal);

            alertaService.verificarVacinaProximaVencimento(animal);

            alertaService.verificarVacinaVencida(animal);

            alertaService.verificarPeriodoCarencia(animal);
        }
    }
}
