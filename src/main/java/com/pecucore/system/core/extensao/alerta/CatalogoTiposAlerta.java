package com.pecucore.system.core.extensao.alerta;

import com.pecucore.system.core.model.TipoAlertaNucleo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CatalogoTiposAlerta {

    @Autowired(required = false)
    private List<ProvedorTiposAlerta> provedores = List.of();

    private Map<String, TipoAlerta> tiposPorCodigo = Map.of();

    @PostConstruct
    void montarCatalogo() {
        List<TipoAlerta> tipos = new ArrayList<>(Arrays.asList(TipoAlertaNucleo.values()));
        provedores.forEach(provedor -> tipos.addAll(provedor.getTipos()));

        Map<String, TipoAlerta> mapa = new LinkedHashMap<>();
        for (TipoAlerta tipo : tipos) {
            TipoAlerta anterior = mapa.putIfAbsent(tipo.getCodigo(), tipo);
            if (anterior != null) {
                throw new IllegalStateException("Código de tipo de alerta repetido: " + tipo.getCodigo());
            }
        }

        tiposPorCodigo = Collections.unmodifiableMap(mapa);
    }

    public List<TipoAlerta> getAllTiposAlerta() {
        return List.copyOf(tiposPorCodigo.values());
    }

    public Optional<TipoAlerta> getTipoAlertaByCodigo(String codigo) {
        return Optional.ofNullable(tiposPorCodigo.get(codigo));
    }
}
