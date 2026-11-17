package com.pecucore.system.core.service.custo;

import com.pecucore.system.core.evento.CustoRegistradoEvento;
import com.pecucore.system.core.model.Custo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CustoEventoPublisher {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void publicarCustoRegistrado(Custo custo) {
        if (custo.getAnimal() != null) {
            eventPublisher.publishEvent(new CustoRegistradoEvento(custo.getAnimal().getId()));
        }
    }
}
