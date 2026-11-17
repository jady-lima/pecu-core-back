package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.model.Alerta;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAlerta;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.repository.AlertaRepository;
import com.pecucore.system.core.service.AnimalService;
import com.pecucore.system.core.extensao.alerta.GatilhoAlerta;
import com.pecucore.system.core.extensao.alerta.OcorrenciaAlerta;
import com.pecucore.system.core.extensao.alerta.VerificadorAlerta;
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
    private AlertaRepository alertaRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void verificar(Long animalId, GatilhoAlerta gatilho) {
        Animal animal = animalService.getAnimalById(animalId);

        verificadores.stream()
                .filter(verificador -> verificador.getGatilhos().contains(gatilho))
                .forEach(verificador -> verificador.avaliar(animal).ifPresentOrElse(
                        ocorrencia -> abrirSeNecessario(animal, verificador.getTipo(), ocorrencia),
                        () -> resolverAbertos(animal, verificador.getTipo())
                ));
    }

    private void abrirSeNecessario(Animal animal, TipoAlerta tipo, OcorrenciaAlerta ocorrencia) {
        boolean existeAtivo = alertaRepository.existsByAnimalIdAndTipoAndStatusIn(
                animal.getId(), tipo, StatusAlerta.ativos());

        boolean usuarioJaFechou = alertaRepository.existsFechadoPeloUsuario(
                animal.getId(), tipo, ocorrencia.referencia(), StatusAlerta.encerrados());

        if (existeAtivo || usuarioJaFechou) {
            return;
        }

        alertaRepository.save(Alerta.abrir(animal, tipo, ocorrencia.referencia(), ocorrencia.mensagem()));
    }

    private void resolverAbertos(Animal animal, TipoAlerta tipo) {
        alertaRepository.findByAnimalIdAndTipoAndStatusIn(animal.getId(), tipo, StatusAlerta.ativos())
                .forEach(Alerta::resolverAutomaticamente);
    }
}
