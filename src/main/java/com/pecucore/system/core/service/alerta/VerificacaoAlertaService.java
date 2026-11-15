package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.evento.PesagemRegistradaEvento;
import com.pecucore.system.core.evento.VacinacaoRegistradaEvento;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.service.AnimalService;
import com.pecucore.system.core.service.alerta.verificador.GatilhoAlerta;
import com.pecucore.system.core.service.alerta.verificador.VerificadorAlerta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerificacaoAlertaService {

    @Autowired
    private List<VerificadorAlerta> verificadores;

    @Autowired
    private AnimalService animalService;

    @EventListener
    public void tratarPesagemRegistrada(PesagemRegistradaEvento evento) {
        verificar(animalService.getAnimalById(evento.animalId()), GatilhoAlerta.PESAGEM_REGISTRADA);
    }

    @EventListener
    public void tratarVacinacaoRegistrada(VacinacaoRegistradaEvento evento) {
        verificar(animalService.getAnimalById(evento.animalId()), GatilhoAlerta.VACINACAO_REGISTRADA);
    }

    public void verificar(Animal animal, GatilhoAlerta gatilho) {
        verificadores.stream()
                .filter(verificador -> verificador.getGatilhos().contains(gatilho))
                .forEach(verificador -> verificador.verificar(animal));
    }
}
