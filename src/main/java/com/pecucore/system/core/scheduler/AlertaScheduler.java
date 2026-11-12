package com.pecucore.system.core.scheduler;

import com.pecucore.system.core.service.alerta.AlertaPesoService;
import com.pecucore.system.core.service.alerta.AlertaSanitarioService;
import com.pecucore.system.core.repository.AnimalRepository;
import com.pecucore.system.core.model.Animal;
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
