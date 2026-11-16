package com.pecucore.system.core.service.politica;

import com.pecucore.system.core.model.Perfil;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
public class PoliticaVisibilidadeFinanceira {

    private static final Set<Perfil> PERFIS_COM_ACESSO_FINANCEIRO = EnumSet.of(Perfil.PRODUTOR);

    public boolean podeVerDadosFinanceiros(Usuario usuario) {
        return usuario != null && PERFIS_COM_ACESSO_FINANCEIRO.contains(usuario.getPerfil());
    }

    public boolean podeVerAlerta(TipoAlerta tipo, Usuario usuario) {
        return !tipo.isFinanceiro() || podeVerDadosFinanceiros(usuario);
    }
}
