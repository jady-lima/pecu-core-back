package com.pecucore.system.bovinos.service.aplicacao;

import com.pecucore.system.core.extensao.aplicacao.DescricaoAplicacao;
import org.springframework.stereotype.Component;

@Component
public class DescricaoAplicacaoBovinos implements DescricaoAplicacao {

    private static final String NOME_SISTEMA = "PecuCore";
    private static final String ESPECIE = "bovinos de corte";

    @Override
    public String getNomeSistema() {
        return NOME_SISTEMA;
    }

    @Override
    public String getEspecie() {
        return ESPECIE;
    }
}
