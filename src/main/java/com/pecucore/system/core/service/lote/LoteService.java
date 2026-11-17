
package com.pecucore.system.core.service.lote;

import com.pecucore.system.core.service.animal.AnimalConsultaService;
import com.pecucore.system.core.service.propriedade.AcessoPropriedadeService;
import com.pecucore.system.core.service.propriedade.PropriedadeService;
import java.util.List;
import java.util.Optional;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.extensao.agrupamento.FaseLote;
import com.pecucore.system.core.extensao.agrupamento.PoliticaAgrupamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pecucore.system.core.dto.lote.LoteMetaDesempenhoRequestDTO;
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
    private AnimalConsultaService animalConsultaService;

    @Autowired
    private AcessoPropriedadeService acessoPropriedadeService;

    @Autowired
    private PoliticaAgrupamento politicaAgrupamento;

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

        validarFinalidade(dados.finalidade());

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

        Lote loteExistente = getLoteById(
                        id,
                        usuarioLogado
                );

        Propriedade propriedade = propriedadeService.getPropriedadeById(
                        dados.propriedadeId(),
                        usuarioLogado
                );

        if (loteExistente.getNumero() != dados.numero() || !loteExistente.getPropriedade().getId().equals(propriedade.getId())) {
            validarNumeroLote(
                dados.numero(),
                propriedade.getId(),
                id
            );
        }

        long animaisAtivos = animalConsultaService.countAnimaisAtivosByLoteId(id);

        if (dados.capacidade() < animaisAtivos) {

            throw new ConflitoNegocioException(
                "A capacidade não pode ser menor que a quantidade de animais ativos no lote (" + animaisAtivos + ")"
            );
        }

        validarFinalidade(dados.finalidade());

        loteExistente.setNumero(dados.numero());
        loteExistente.setFinalidade(dados.finalidade());
        loteExistente.setCapacidade(dados.capacidade());
        loteExistente.setDataCriacao(dados.dataCriacao());
        loteExistente.setPropriedade(propriedade);

        return loteRepository.save(loteExistente);
    }

    public List<Lote> getAllLotes(Usuario usuarioLogado) {
        return acessoPropriedadeService.filtrarPorPerfil(
                usuarioLogado,
                loteRepository::findAll,
                loteRepository::findByPropriedadeUsuarioId,
                loteRepository::findByPropriedadeVeterinariosId
        );
    }

    public Lote getLoteById(Long id, Usuario usuarioLogado) {

        Lote lote = loteRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));

        validarAcessoLote(lote, usuarioLogado);

        return lote;
    }

    public Lote updateMetaDesempenho(Long id, LoteMetaDesempenhoRequestDTO dados, Usuario usuarioLogado) {
        Lote lote = getLoteById(
                id,
                usuarioLogado
        );

        lote.setMetaDesempenho(dados.metaDesempenho());

        return loteRepository.save(lote);
    }

    public void deleteMetaDesempenho(Long id, Usuario usuarioLogado) {
        Lote lote = getLoteById(
                id,
                usuarioLogado
        );

        lote.setMetaDesempenho(null);

        loteRepository.save(lote);
    }

    public void deleteLote(Long id, Usuario usuarioLogado) {

        Lote loteExistente = getLoteById(
                        id,
                        usuarioLogado
                );

        boolean possuiAnimaisAtivos = animalConsultaService.existsAnimalAtivoByLoteId(id);

        if (possuiAnimaisAtivos) {
            throw new ConflitoNegocioException("Não é possível excluir o lote porque existem animais ativos nesse lote");
        }

        loteRepository.delete(loteExistente);
    }

    public List<FaseLote> getAllFasesLote() {
        return politicaAgrupamento.getFases();
    }

    public Optional<Double> getMetaDesempenho(Lote lote) {
        if (lote == null) {
            return Optional.empty();
        }

        if (lote.getMetaDesempenho() != null) {
            return Optional.of(lote.getMetaDesempenho());
        }

        if (lote.getFinalidade() == null) {
            return Optional.empty();
        }

        return politicaAgrupamento.getMetaDaFase(lote.getFinalidade());
    }

    private void validarNumeroLote(int numero, Long propriedadeId, Long loteIdAtual
    ) {

        boolean numeroJaExiste;

        if (loteIdAtual == null) {
            numeroJaExiste = loteRepository.existsByNumeroAndPropriedade_Id(
                        numero,
                        propriedadeId
                );
        } else {
            numeroJaExiste = loteRepository.existsByNumeroAndPropriedade_IdAndIdNot(
                        numero,
                        propriedadeId,
                        loteIdAtual
                );
        }

        if (numeroJaExiste) {
            throw new ConflitoNegocioException("Já existe um lote com esse número nesta propriedade");
        }
    }

    private void validarAcessoLote(Lote lote, Usuario usuarioLogado) {
        acessoPropriedadeService.validarAcesso(
                lote.getPropriedade(),
                usuarioLogado,
                "Você não tem acesso a este lote"
        );
    }

    private void validarFinalidade(String finalidade) {
        List<String> codigos = politicaAgrupamento.getFases().stream()
                .map(FaseLote::codigo)
                .toList();

        if (!codigos.contains(finalidade)) {
            throw new RegraNegocioException(
                    "Finalidade inválida: " + finalidade + ". Valores aceitos: " + String.join(", ", codigos) + "."
            );
        }
    }
}
