package com.pecucore.system.scheduler;

import com.pecucore.system.service.alerta.AlertaPesoService;
import com.pecucore.system.service.alerta.AlertaSanitarioService;
import com.pecucore.system.repository.AnimalRepository;
import com.pecucore.system.model.Animal;
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
