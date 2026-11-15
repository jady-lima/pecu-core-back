package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.exception.AcessoNegadoException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Perfil;
import com.pecucore.system.core.model.Propriedade;
import com.pecucore.system.core.model.RegraAlerta;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.PropriedadeRepository;
import com.pecucore.system.core.repository.RegraAlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegraAlertaService {

    @Autowired
    private RegraAlertaRepository regraAlertaRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    public RegraAlerta createRegraAlerta(Long propriedadeId, TipoAlerta tipo, double parametro) {
        validarRegra(tipo, parametro);

        Propriedade propriedade = propriedadeRepository.findById(propriedadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Propriedade não encontrada"));

        validarAcesso(propriedade, tipo);

        if (regraAlertaRepository.existsByPropriedadeIdAndTipo(propriedadeId, tipo)) {
            throw new RegraNegocioException("Já existe uma regra de " + tipo + " para esta propriedade. Atualize a regra existente.");
        }

        RegraAlerta regra = new RegraAlerta();
        regra.setPropriedade(propriedade);
        regra.setTipo(tipo);
        regra.setParametro(parametro);
        regra.setAtiva(true);

        return regraAlertaRepository.save(regra);
    }

    public RegraAlerta updateRegraAlerta(Long id, double parametro, boolean ativa) {
        RegraAlerta regra = regraAlertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Regra de alerta não encontrada"));

        validarRegra(regra.getTipo(), parametro);
        validarAcesso(regra.getPropriedade(), regra.getTipo());

        regra.setParametro(parametro);
        regra.setAtiva(ativa);

        return regraAlertaRepository.save(regra);
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

    private void validarAcesso(Propriedade propriedade, TipoAlerta tipo) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Usuario)) {
            throw new AcessoNegadoException("Usuário não autenticado.");
        }

        Usuario usuario = (Usuario) autenticacao.getPrincipal();

        if (usuario.getPerfil() == Perfil.ADMIN) {
            return;
        }

        if (usuario.getPerfil() == Perfil.VETERINARIO && tipo.isFinanceiro()) {
            throw new AcessoNegadoException("Você não tem permissão para gerenciar regras financeiras.");
        }

        boolean produtorResponsavel = propriedade.getUsuario() != null && propriedade.getUsuario().getId().equals(usuario.getId());

        boolean veterinarioAssociado = propriedade.getVeterinarios() != null && propriedade.getVeterinarios().stream().anyMatch(v -> v.getId().equals(usuario.getId()));

        if (!produtorResponsavel && !veterinarioAssociado) {
            throw new AcessoNegadoException("Você não tem acesso a esta propriedade");
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
            throw new RegraNegocioException(
                    "O parâmetro deve ser " + tipo.getUnidadeParametro().getRegraValor() + ".");
        }
    }
}
