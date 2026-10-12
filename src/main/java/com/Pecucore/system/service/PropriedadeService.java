package com.Pecucore.system.service;

import com.Pecucore.system.dto.PropriedadeRequestDTO;
import com.Pecucore.system.model.Perfil;
import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.PropriedadeRepository;
import com.Pecucore.system.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PropriedadeService {

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Propriedade create(PropriedadeRequestDTO dados, Usuario usuarioLogado) {

        Usuario dono = definirDono(dados, usuarioLogado);

        Propriedade propriedade = new Propriedade();

        propriedade.setNome(dados.nome());
        propriedade.setLocalizacao(dados.localizacao());
        propriedade.setUsuario(dono);

        return propriedadeRepository.save(propriedade);
    }

    private Usuario definirDono(
            PropriedadeRequestDTO dados,
            Usuario usuarioLogado
    ) {

        if (usuarioLogado.getPerfil() == Perfil.ADMIN) {

            if (dados.usuarioId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O usuário dono da propriedade deve ser informado"
                );
            }

            Usuario dono = getUsuarioById(dados.usuarioId());

            if (dono.getPerfil() != Perfil.PRODUTOR) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A propriedade só pode pertencer a um usuário com perfil PRODUTOR"
                );
            }

            return dono;
        }

        if (dados.usuarioId() != null
                && !dados.usuarioId().equals(usuarioLogado.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Um produtor só pode cadastrar propriedades em seu próprio nome"
            );
        }

        return getUsuarioById(usuarioLogado.getId());
    }

    private Usuario getUsuarioById(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));
    }

    public Propriedade update(
            Long id,
            PropriedadeRequestDTO dados,
            Usuario usuarioLogado
    ) {

        Propriedade propriedadeExistente =
                getPropriedadeComAcesso(id, usuarioLogado);

        propriedadeExistente.setNome(dados.nome());
        propriedadeExistente.setLocalizacao(dados.localizacao());

        return propriedadeRepository.save(propriedadeExistente);
    }

    public List<Propriedade> getAllPropriedades(
            Usuario usuarioLogado
    ) {

        if (usuarioLogado.getPerfil() == Perfil.PRODUTOR) {
            return propriedadeRepository.findByUsuarioId(
                    usuarioLogado.getId()
            );
        }

        return propriedadeRepository.findAll();
    }

    public Propriedade getPropriedadeById(
            Long id,
            Usuario usuarioLogado
    ) {

        return getPropriedadeComAcesso(id, usuarioLogado);
    }

    public void deletePropriedade(
            Long id,
            Usuario usuarioLogado
    ) {

        Propriedade propriedadeExistente =
                getPropriedadeComAcesso(id, usuarioLogado);

        propriedadeRepository.delete(propriedadeExistente);
    }

    private Propriedade getPropriedadeComAcesso(
            Long id,
            Usuario usuarioLogado
    ) {

        Propriedade propriedade =
                propriedadeRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Propriedade não encontrada"
                        ));

        if (usuarioLogado.getPerfil() == Perfil.PRODUTOR) {

            if (propriedade.getUsuario() == null
                    || !propriedade.getUsuario().getId()
                    .equals(usuarioLogado.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Você não tem acesso a esta propriedade"
                );
            }
        }

        return propriedade;
    }
}