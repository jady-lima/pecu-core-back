
package com.pecucore.system.core.service;

import java.util.List;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pecucore.system.core.dto.lote.LoteMetaGmdRequestDTO;
import com.pecucore.system.core.dto.lote.LoteRequestDTO;
import com.pecucore.system.core.model.Lote;
import com.pecucore.system.core.model.Propriedade;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.LoteRepository;

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

    public Lote createLote(
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

    public Lote updateLote(
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

            throw new ConflitoNegocioException(
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

        return acessoPropriedadeService.filtrarPorPerfil(
                usuarioLogado,
                loteRepository::findAll,
                loteRepository::findByPropriedadeUsuarioId,
                loteRepository::findByPropriedadeVeterinariosId
        );
    }

    public Lote getLoteById(
            Long id,
            Usuario usuarioLogado
    ) {

        Lote lote =
                loteRepository.findById(id)
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Lote não encontrado"
                        ));

        validarAcessoLote(
                lote,
                usuarioLogado
        );

        return lote;
    }

    public Lote updateMetaGmd(Long id, LoteMetaGmdRequestDTO dados, Usuario usuarioLogado) {
        Lote lote = getLoteById(
                id,
                usuarioLogado
        );

        lote.setGmdMeta(dados.gmdEsperado());

        return loteRepository.save(lote);
    }

    public void deleteMetaGmd(Long id, Usuario usuarioLogado) {
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

            throw new ConflitoNegocioException(
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
            throw new ConflitoNegocioException(
                    "Já existe um lote com esse número nesta propriedade"
            );
        }
    }

    private void validarAcessoLote(
            Lote lote,
            Usuario usuarioLogado
    ) {

        acessoPropriedadeService.validarAcesso(
                lote.getPropriedade(),
                usuarioLogado,
                "Você não tem acesso a este lote"
        );
    }
}
