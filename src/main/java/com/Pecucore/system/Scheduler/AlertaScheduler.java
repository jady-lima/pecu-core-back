package com.Pecucore.system.Scheduler;

import com.Pecucore.system.service.AlertaPesoService;
import com.Pecucore.system.service.AlertaSanitarioService;
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
    private AlertaSanitarioService alertaSanitarioService;

    @Autowired
    private AlertaPesoService alertaPesoService;

    @Scheduled(cron = "0 0 7 * * *")
    public void verificarAlertas() {

        for (Animal animal : animalRepository.findAll()) {
            alertaPesoService.verificarPesagemAtrasada(animal);

            alertaSanitarioService.verificarVacinaProximaVencimento(animal);

            alertaSanitarioService.verificarVacinaVencida(animal);

            alertaSanitarioService.verificarPeriodoCarencia(animal);
        }
    }
}
