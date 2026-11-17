package com.pecucore.system.core.model.conversor;

import com.pecucore.system.core.extensao.alerta.CatalogoTiposAlerta;
import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;

@Converter
public class TipoAlertaConverter implements AttributeConverter<TipoAlerta, String> {

    @Autowired
    private CatalogoTiposAlerta catalogoTiposAlerta;

    @Override
    public String convertToDatabaseColumn(TipoAlerta tipo) {
        return tipo != null ? tipo.getCodigo() : null;
    }

    @Override
    public TipoAlerta convertToEntityAttribute(String codigo) {
        if (codigo == null) {
            return null;
        }

        return catalogoTiposAlerta.getTipoAlertaByCodigo(codigo)
                .orElseThrow(() -> new IllegalStateException(
                        "Tipo de alerta '" + codigo + "' não é fornecido pela aplicação ativa."
                ));
    }
}
