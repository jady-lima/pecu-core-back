
package com.Pecucore.system.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.LoteRequestDTO;
import com.Pecucore.system.model.Lote;
import com.Pecucore.system.model.Perfil;
import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.model.StatusAnimal;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.AnimalRepository;
import com.Pecucore.system.repository.LoteRepository;
import com.Pecucore.system.repository.PropriedadeRepository;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AcessoPropriedadeService acessoPropriedadeService;

    public Lote create(
            LoteRequestDTO dados,
            Usuario usuarioLogado
    ) {

        Propriedade propriedade =
                propriedadeRepository.findById(dados.propriedadeId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Propriedade não encontrada"
                        ));

        validarAcessoPropriedade(
                propriedade,
                usuarioLogado
        );

        validarNumeroLote(
                dados.numero(),
                propriedade.getId(),
                null
        );

        Lote lote = new Lote();

        lote.setNumero(dados.numero());
        lote.setFinalidade(dados.finalidade());
        lote.setCapacidade(dados.capacidade());
        lote.setDataCriacao(dados.dataCriacao());
        lote.setPropriedade(propriedade);

        return loteRepository.save(lote);
    }

    public Lote update(
            Long id,
            LoteRequestDTO dados,
            Usuario usuarioLogado
    ) {

        Lote loteExistente =
                loteRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Lote não encontrado"
                        ));

        validarAcessoLote(
                loteExistente,
                usuarioLogado
        );

        Propriedade propriedade =
                propriedadeRepository.findById(
                        dados.propriedadeId()
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Propriedade não encontrada"
                ));

        validarAcessoPropriedade(
                propriedade,
                usuarioLogado
        );

        if (loteExistente.getNumero() != dados.numero()
                || !loteExistente.getPropriedade().getId().equals(propriedade.getId())) {

            validarNumeroLote(
                    dados.numero(),
                    propriedade.getId(),
                    id
            );
        }

        loteExistente.setNumero(dados.numero());
        loteExistente.setFinalidade(dados.finalidade());
        loteExistente.setCapacidade(dados.capacidade());
        loteExistente.setDataCriacao(dados.dataCriacao());
        loteExistente.setPropriedade(propriedade);

        return loteRepository.save(loteExistente);
    }

    public List<Lote> getAllLotes(
            Usuario usuarioLogado
    ) {

        if (usuarioLogado.getPerfil() == Perfil.PRODUTOR) {
            return loteRepository.findByPropriedadeUsuarioId(
                    usuarioLogado.getId()
            );
        }

        if (usuarioLogado.getPerfil() == Perfil.VETERINARIO) {
            return loteRepository.findByPropriedadeVeterinariosId(usuarioLogado.getId());
        }

        return loteRepository.findAll();
    }

    public Lote getLoteById(
            Long id,
            Usuario usuarioLogado
    ) {

        Lote lote =
                loteRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Lote não encontrado"
                        ));

        validarAcessoLote(
                lote,
                usuarioLogado
        );

        return lote;
    }

    public void deleteLote(
            Long id,
            Usuario usuarioLogado
    ) {

        Lote loteExistente =
                loteRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Lote não encontrado"
                        ));

        validarAcessoLote(
                loteExistente,
                usuarioLogado
        );

        boolean possuiAnimaisAtivos =
                animalRepository.existsByLoteIdAndStatus(
                        id,
                        StatusAnimal.ATIVO
                );

        if (possuiAnimaisAtivos) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir o lote porque existem animais ativos nesse lote"
            );
        }

        loteRepository.delete(loteExistente);
    }

    private void validarNumeroLote(
            int numero,
            Long propriedadeId,
            Long loteIdAtual
    ) {

        boolean numeroJaExiste;

        if (loteIdAtual == null) {
            numeroJaExiste =
                    loteRepository.existsByNumeroAndPropriedade_Id(
                            numero,
                            propriedadeId
                    );
        } else {
            numeroJaExiste =
                    loteRepository.existsByNumeroAndPropriedade_IdAndIdNot(
                            numero,
                            propriedadeId,
                            loteIdAtual
                    );
        }

        if (numeroJaExiste) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um lote com esse número nesta propriedade"
            );
        }
    }

    private void validarAcessoPropriedade(
            Propriedade propriedade,
            Usuario usuarioLogado
    ) {

        if (!acessoPropriedadeService.possuiAcesso(propriedade, usuarioLogado)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem acesso a esta propriedade"
            );
        }
    }

    private void validarAcessoLote(
            Lote lote,
            Usuario usuarioLogado
    ) {

        if (!acessoPropriedadeService.possuiAcesso(lote.getPropriedade(),usuarioLogado)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem acesso a este lote"
            );
        }
    }
}
