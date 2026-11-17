package com.pecucore.system.core.scheduler;

import com.pecucore.system.core.service.AnimalService;
import com.pecucore.system.core.service.alerta.VerificacaoAlertaService;
import com.pecucore.system.core.extensao.alerta.GatilhoAlerta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlertaScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertaScheduler.class);

    @Autowired
    private AnimalService animalService;

    @Autowired
    private VerificacaoAlertaService verificacaoAlertaService;

    @Scheduled(cron = "${alerta.rotina-diaria.cron:0 0 7 * * *}")
    public void verificarAlertas() {
        List<Long> animalIds = animalService.getIdsAtivosComLote();
        int falhas = 0;

        for (Long animalId : animalIds) {
            try {
                verificacaoAlertaService.verificar(animalId, GatilhoAlerta.ROTINA_DIARIA);
            } catch (RuntimeException e) {
                falhas++;
                log.error("Falha ao verificar alertas do animal {} na rotina diária", animalId, e);
            }
        }

        log.info("Rotina diária de alertas concluída: {} animais verificados, {} com falha",
                animalIds.size(), falhas);
    }
}
