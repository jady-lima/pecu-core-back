package com.pecucore.system.core.service;

import com.pecucore.system.core.exception.AcessoNegadoException;
import com.pecucore.system.core.model.Perfil;
import com.pecucore.system.core.model.Propriedade;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.PropriedadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class AcessoPropriedadeService {

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    public boolean possuiAcesso(Propriedade propriedade, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() == Perfil.ADMIN) {
            return true;
        }

        if (propriedade == null) {
            return false;
        }

        if (usuarioLogado.getPerfil() == Perfil.PRODUTOR) {
            return propriedade.getUsuario() != null && propriedade.getUsuario().getId().equals(usuarioLogado.getId());
        }

        if (usuarioLogado.getPerfil() == Perfil.VETERINARIO) {
            return propriedadeRepository.existsByIdAndVeterinariosId(
                propriedade.getId(),
                usuarioLogado.getId()
            );
        }

        return false;
    }

    public void validarAcesso(Propriedade propriedade, Usuario usuarioLogado, String mensagem) {
        if (!possuiAcesso(propriedade, usuarioLogado)) {
            throw new AcessoNegadoException(mensagem);
        }
    }

    public <T> List<T> filtrarPorPerfil(
            Usuario usuarioLogado,
            Supplier<List<T>> todos,
            Function<Long, List<T>> doProdutor,
            Function<Long, List<T>> doVeterinario
    ) {
        return switch (usuarioLogado.getPerfil()) {
            case ADMIN -> todos.get();
            case PRODUTOR -> doProdutor.apply(usuarioLogado.getId());
            case VETERINARIO -> doVeterinario.apply(usuarioLogado.getId());
        };
    }
}
