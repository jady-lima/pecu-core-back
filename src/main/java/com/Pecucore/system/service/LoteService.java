
package com.Pecucore.system.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.LoteMetaGmdRequestDTO;
import com.Pecucore.system.dto.LoteRequestDTO;
import com.Pecucore.system.model.Lote;
import com.Pecucore.system.model.Perfil;
import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.LoteRepository;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private PropriedadeService propriedadeService;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private AcessoPropriedadeService acessoPropriedadeService;

    public Lote create(
            LoteRequestDTO dados,
            Usuario usuarioLogado
    ) {

        Propriedade propriedade =
                propriedadeService.getPropriedadeById(
                        dados.propriedadeId(),
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
                getLoteById(
                        id,
                        usuarioLogado
                );

        Propriedade propriedade =
                propriedadeService.getPropriedadeById(
                        dados.propriedadeId(),
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

        long animaisAtivos =
                animalService.countAnimaisAtivosByLoteId(id);

        if (dados.capacidade() < animaisAtivos) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A capacidade não pode ser menor que a quantidade de animais ativos no lote (" + animaisAtivos + ")"
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

    public Lote atualizarMetaGmd(Long id, LoteMetaGmdRequestDTO dados, Usuario usuarioLogado) {
        Lote lote = getLoteById(
                id,
                usuarioLogado
        );

        lote.setGmdMeta(dados.gmdEsperado());

        return loteRepository.save(lote);
    }

    public void removerMetaGmd(Long id,Usuario usuarioLogado) {
        Lote lote = getLoteById(
                        id,
                        usuarioLogado
                );

        lote.setGmdMeta(null);

        loteRepository.save(lote);
    }

    public void deleteLote(
            Long id,
            Usuario usuarioLogado
    ) {

        Lote loteExistente =
                getLoteById(
                        id,
                        usuarioLogado
                );

        boolean possuiAnimaisAtivos =
                animalService.existsAnimalAtivoByLoteId(id);

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
