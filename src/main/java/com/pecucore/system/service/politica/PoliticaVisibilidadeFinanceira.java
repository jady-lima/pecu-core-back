package com.pecucore.system.service.politica;

import com.pecucore.system.model.Perfil;
import com.pecucore.system.model.TipoAlerta;
import com.pecucore.system.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
public class PoliticaVisibilidadeFinanceira {

    private static final Set<Perfil> PERFIS_COM_ACESSO_FINANCEIRO = EnumSet.of(Perfil.PRODUTOR);

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
