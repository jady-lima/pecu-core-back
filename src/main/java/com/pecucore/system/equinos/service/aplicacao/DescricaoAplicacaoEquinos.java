package com.pecucore.system.equinos.service.aplicacao;

import com.pecucore.system.core.extensao.aplicacao.DescricaoAplicacao;
import org.springframework.stereotype.Component;

@Component
public class DescricaoAplicacaoEquinos implements DescricaoAplicacao {

    private static final String NOME_SISTEMA = "EquiCore";
    private static final String ESPECIE = "equinos";

    @Override
    public String getNomeSistema() {
        return NOME_SISTEMA;
    }

    @Override
    public String getEspecie() {
        return ESPECIE;
    }
}