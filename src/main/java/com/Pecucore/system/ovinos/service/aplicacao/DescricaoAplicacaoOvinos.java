package com.Pecucore.system.ovinos.service.aplicacao;

import com.pecucore.system.core.extensao.aplicacao.DescricaoAplicacao;
import org.springframework.stereotype.Component;



@Component
public class DescricaoAplicacaoOvinos implements DescricaoAplicacao{

    private static final String NOME_SISTEMA = "OvinoCore";
    private static final String ESPECIE = "ovinos";


    @Override
    public String getNomeSistema() {
        return NOME_SISTEMA;
    }

    @Override
    public String getEspecie() {
        return ESPECIE;
    }
}
