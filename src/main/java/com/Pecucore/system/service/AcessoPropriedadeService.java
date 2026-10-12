package com.Pecucore.system.service;

import com.Pecucore.system.model.Perfil;
import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.PropriedadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
