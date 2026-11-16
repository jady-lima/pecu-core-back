package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.evento.CustoRegistradoEvento;
import com.pecucore.system.core.evento.MedicamentoRegistradoEvento;
import com.pecucore.system.core.evento.PesagemRegistradaEvento;
import com.pecucore.system.core.evento.VacinacaoRegistradaEvento;
import com.pecucore.system.core.service.alerta.verificador.GatilhoAlerta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AlertaEventoListener {

    private static final Logger log = LoggerFactory.getLogger(AlertaEventoListener.class);

    @Autowired
    private VerificacaoAlertaService verificacaoAlertaService;

    @TransactionalEventListener(fallbackExecution = true)
    public void tratarPesagemRegistrada(PesagemRegistradaEvento evento) {
        verificarComSeguranca(evento.animalId(), GatilhoAlerta.PESAGEM_REGISTRADA);
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void tratarVacinacaoRegistrada(VacinacaoRegistradaEvento evento) {
        verificarComSeguranca(evento.animalId(), GatilhoAlerta.VACINACAO_REGISTRADA);
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void tratarMedicamentoRegistrado(MedicamentoRegistradoEvento evento) {
        verificarComSeguranca(evento.animalId(), GatilhoAlerta.MEDICAMENTO_REGISTRADO);
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void tratarCustoRegistrado(CustoRegistradoEvento evento) {
        verificarComSeguranca(evento.animalId(), GatilhoAlerta.CUSTO_REGISTRADO);
    }

    private void verificarComSeguranca(Long animalId, GatilhoAlerta gatilho) {
        try {
            verificacaoAlertaService.verificar(animalId, gatilho);
        } catch (RuntimeException e) {
            log.error("Falha ao verificar alertas do animal {} (gatilho {})", animalId, gatilho, e);
        }
    }
}
