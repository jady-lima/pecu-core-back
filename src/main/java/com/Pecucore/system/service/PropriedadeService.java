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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PropriedadeService {

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AcessoPropriedadeService acessoPropriedadeService;

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

        if (usuarioLogado.getPerfil() == Perfil.VETERINARIO) {
            return propriedadeRepository.findByVeterinariosId(
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

        if (!acessoPropriedadeService.possuiAcesso(propriedade, usuarioLogado)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem acesso a esta propriedade"
            );
        }

        return propriedade;
    }

    @Transactional
    public Propriedade vincularVeterinario(Long id, Long veterinarioId, Usuario usuarioLogado) {

        Propriedade propriedade = getPropriedadeComAcesso(id, usuarioLogado);
        Usuario veterinario = getUsuarioById(veterinarioId);

        if (veterinario.getPerfil() != Perfil.VETERINARIO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Apenas usuários com perfil VETERINARIO podem ser vinculados à propriedade"
            );
        }
        propriedade.getVeterinarios().add(veterinario);

        return propriedadeRepository.save(propriedade);
    }

    @Transactional
    public Propriedade desvincularVeterinario(Long id, Long veterinarioId, Usuario usuarioLogado) {

        Propriedade propriedade = getPropriedadeComAcesso(id, usuarioLogado);

        boolean removido = propriedade.getVeterinarios().removeIf(veterinario -> veterinario.getId().equals(veterinarioId));

        if (!removido) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Veterinário não está vinculado a esta propriedade"
            );
        }

        return propriedadeRepository.save(propriedade);
    }

    @Transactional(readOnly = true)
    public List<Usuario> getVeterinarios(Long id, Usuario usuarioLogado) {
        Propriedade propriedade = getPropriedadeComAcesso(id, usuarioLogado);
        return List.copyOf(propriedade.getVeterinarios());
    }
}