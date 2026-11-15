package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.dto.alerta.RegraAlertaRequestDTO;
import com.pecucore.system.core.dto.alerta.RegraAlertaUpdateRequestDTO;
import com.pecucore.system.core.exception.AcessoNegadoException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Propriedade;
import com.pecucore.system.core.model.RegraAlerta;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.RegraAlertaRepository;
import com.pecucore.system.core.service.AcessoPropriedadeService;
import com.pecucore.system.core.service.PropriedadeService;
import com.pecucore.system.core.service.politica.PoliticaVisibilidadeFinanceira;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegraAlertaService {

    @Autowired
    private RegraAlertaRepository regraAlertaRepository;

    @Autowired
    private PropriedadeService propriedadeService;

    @Autowired
    private AcessoPropriedadeService acessoPropriedadeService;

    @Autowired
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira;

    @Transactional
    public RegraAlerta createRegraAlerta(RegraAlertaRequestDTO dados, Usuario usuarioLogado) {
        validarRegra(dados.tipo(), dados.parametro());

        Propriedade propriedade = propriedadeService.getPropriedadeById(dados.propriedadeId(), usuarioLogado);

        validarPermissaoFinanceira(dados.tipo(), usuarioLogado);

        if (regraAlertaRepository.existsByPropriedadeIdAndTipo(propriedade.getId(), dados.tipo())) {
            throw new RegraNegocioException("Já existe uma regra de " + dados.tipo()
                    + " para esta propriedade. Atualize a regra existente.");
        }

        RegraAlerta regra = new RegraAlerta();
        regra.setPropriedade(propriedade);
        regra.setTipo(dados.tipo());
        regra.setParametro(dados.parametro());
        regra.setAtiva(true);

        return regraAlertaRepository.save(regra);
    }

    @Transactional
    public RegraAlerta updateRegraAlerta(Long id, RegraAlertaUpdateRequestDTO dados, Usuario usuarioLogado) {
        RegraAlerta regra = regraAlertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Regra de alerta não encontrada"));

        validarRegra(regra.getTipo(), dados.parametro());

        acessoPropriedadeService.validarAcesso(
                regra.getPropriedade(),
                usuarioLogado,
                "Você não tem acesso a esta propriedade"
        );
        validarPermissaoFinanceira(regra.getTipo(), usuarioLogado);

        regra.setParametro(dados.parametro());
        regra.setAtiva(dados.ativa());

        return regraAlertaRepository.save(regra);
    }

    public List<RegraAlerta> getAllRegrasAlerta(Long propriedadeId, Usuario usuarioLogado) {
        Propriedade propriedade = propriedadeService.getPropriedadeById(propriedadeId, usuarioLogado);

        return regraAlertaRepository.findByPropriedadeIdOrderByTipo(propriedade.getId()).stream()
                .filter(regra -> politicaVisibilidadeFinanceira.podeVerAlerta(regra.getTipo(), usuarioLogado))
                .toList();
    }

    public Optional<Double> getParametroAtivo(Animal animal, TipoAlerta tipo) {
        if (animal.getLote() == null || animal.getLote().getPropriedade() == null) {
            return tipo.getParametroPadrao();
        }

        List<RegraAlerta> regras = regraAlertaRepository.findByPropriedadeIdAndTipo(
                animal.getLote().getPropriedade().getId(), tipo);

        if (regras.isEmpty()) {
            return tipo.getParametroPadrao();
        }

        return regras.stream()
                .filter(RegraAlerta::isAtiva)
                .map(RegraAlerta::getParametro)
                .findFirst();
    }

    private void validarPermissaoFinanceira(TipoAlerta tipo, Usuario usuarioLogado) {
        if (tipo.isFinanceiro() && !politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuarioLogado)) {
            throw new AcessoNegadoException("Você não tem permissão para gerenciar regras financeiras.");
        }
    }

    private void validarRegra(TipoAlerta tipo, double parametro) {
        if (tipo == null) {
            throw new RegraNegocioException("O tipo de alerta deve ser informado.");
        }

        if (!tipo.isConfiguravel()) {
            throw new RegraNegocioException("Essa regra é fixa do sistema e não pode ser configurada.");
        }

        if (!tipo.getUnidadeParametro().aceita(parametro)) {
            throw new RegraNegocioException("O parâmetro deve ser " + tipo.getUnidadeParametro().getRegraValor() + ".");
        }
    }
}
