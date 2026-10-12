package com.Pecucore.system.service;

import com.Pecucore.system.model.Perfil;
import com.Pecucore.system.model.TipoAlerta;
import com.Pecucore.system.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
public class PoliticaVisibilidadeFinanceira {

    private static final Set<Perfil> PERFIS_COM_ACESSO_FINANCEIRO = EnumSet.of(Perfil.ADMIN, Perfil.PRODUTOR);

    private static final Set<TipoAlerta> ALERTAS_FINANCEIROS = EnumSet.of(TipoAlerta.CUSTO_ACIMA);

    public boolean podeVerDadosFinanceiros(Usuario usuario) {
        return usuario != null && PERFIS_COM_ACESSO_FINANCEIRO.contains(usuario.getPerfil());
    }

    public boolean podeVerAlerta(TipoAlerta tipo, Usuario usuario) {
        if (!ALERTAS_FINANCEIROS.contains(tipo)) {
            return true;
        }

        return podeVerDadosFinanceiros(usuario);
    }
}
