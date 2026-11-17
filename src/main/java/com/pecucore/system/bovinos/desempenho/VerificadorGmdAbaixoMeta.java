package com.pecucore.system.bovinos.desempenho;

import com.pecucore.system.bovinos.agrupamento.MetaDesempenhoService;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.alerta.RegraAlertaService;
import com.pecucore.system.core.service.alerta.verificador.GatilhoAlerta;
import com.pecucore.system.core.service.alerta.verificador.FormatadorAlerta;
import com.pecucore.system.core.service.alerta.verificador.OcorrenciaAlerta;
import com.pecucore.system.core.service.alerta.verificador.VerificadorAlerta;
import com.pecucore.system.core.service.pesagem.PesagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class VerificadorGmdAbaixoMeta implements VerificadorAlerta {

    @Autowired
    private PesagemService pesagemService;

    @Autowired
    private MetaDesempenhoService metaDesempenhoService;

    @Autowired
    private RegraAlertaService regraAlertaService;

    @Autowired
    private IndicadorDesempenho indicadorDesempenho;

    @Override
    public TipoAlerta getTipo() {
        return TipoAlerta.GMD_ABAIXO_META;
    }

    @Override
    public Set<GatilhoAlerta> getGatilhos() {
        return Set.of(GatilhoAlerta.PESAGEM_REGISTRADA);
    }

    @Override
    public Optional<OcorrenciaAlerta> avaliar(Animal animal) {
        if (regraAlertaService.isAlertaDesativado(animal, getTipo())) {
            return Optional.empty();
        }

        Optional<Double> meta = metaDesempenhoService.getGmdEsperado(animal.getLote())
                .or(() -> regraAlertaService.getParametroAtivo(animal, getTipo()));
        Optional<Pesagem> ultima = pesagemService.getUltimaPesagem(animal.getId());

        if (meta.isEmpty() || ultima.isEmpty()) {
            return Optional.empty();
        }

        Double gmd = ultima.get().getIndiceDesempenho();

        if (gmd == null || gmd >= meta.get()) {
            return Optional.empty();
        }

        return Optional.of(new OcorrenciaAlerta("pesagem:" + ultima.get().getId(),
                "GMD abaixo da meta: o animal ganhou " + formatar(gmd)
                        + ", abaixo da meta de " + formatar(meta.get()) + "."));
    }

    private String formatar(double valor) {
        return FormatadorAlerta.indice(valor, indicadorDesempenho.getUnidade());
    }
}
