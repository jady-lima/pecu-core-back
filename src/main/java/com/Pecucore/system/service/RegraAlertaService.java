package com.Pecucore.system.service;

import com.Pecucore.system.model.Propriedade;
import com.Pecucore.system.model.RegraAlerta;
import com.Pecucore.system.model.TipoAlerta;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.model.Perfil;
import com.Pecucore.system.repository.PropriedadeRepository;
import com.Pecucore.system.repository.RegraAlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class RegraAlertaService {

    @Autowired
    private RegraAlertaRepository regraAlertaRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    public RegraAlerta criarRegra(
            Long propriedadeId,
            TipoAlerta tipo,
            double parametro
    ) {
        validarRegra(tipo, parametro);

        Propriedade propriedade = propriedadeRepository.findById(propriedadeId)
                .orElseThrow(() ->
                        new RuntimeException("Propriedade nao encontrada"));

        validarAcesso(propriedade, tipo);

        RegraAlerta regra = new RegraAlerta();
        regra.setPropriedade(propriedade);
        regra.setTipo(tipo);
        regra.setParametro(parametro);
        regra.setAtiva(true);

        return regraAlertaRepository.save(regra);
    }

    public RegraAlerta atualizarRegra(
            Long id,
            double parametro,
            boolean ativa
    ) {
        RegraAlerta regra = regraAlertaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Regra de alerta não encontrada"));

        validarRegra(regra.getTipo(), parametro);
        validarAcesso(regra.getPropriedade(), regra.getTipo());

        regra.setParametro(parametro);
        regra.setAtiva(ativa);

        return regraAlertaRepository.save(regra);
    }

    private void validarAcesso(Propriedade propriedade, TipoAlerta tipo) {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null
                || !(autenticacao.getPrincipal() instanceof Usuario)) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        Usuario usuario = (Usuario) autenticacao.getPrincipal();

        if (usuario.getPerfil() == Perfil.ADMIN) {
            return;
        }

        if (usuario.getPerfil() == Perfil.VETERINARIO
                && tipo == TipoAlerta.CUSTO_ACIMA) {
            throw new AccessDeniedException(
                    "Você não tem permissão para gerenciar regras financeiras.");
        }

        boolean produtorResponsavel =
                propriedade.getUsuario() != null
                        && propriedade.getUsuario().getId().equals(usuario.getId());

        boolean veterinarioAssociado =
                propriedade.getVeterinarios() != null
                        && propriedade.getVeterinarios().stream()
                        .anyMatch(v -> v.getId().equals(usuario.getId()));

        if (!produtorResponsavel && !veterinarioAssociado) {
            throw new AccessDeniedException(
                    "Você não tem acesso a esta propriedade.");
        }
    }

    private void validarRegra(TipoAlerta tipo, double parametro) {
        if (tipo == null) {
            throw new IllegalArgumentException(
                    "O tipo de alerta deve ser informado.");
        }

        if (!Double.isFinite(parametro) || parametro < 0) {
            throw new IllegalArgumentException(
                    "O parâmetro deve ser válido e não negativo.");
        }

        switch (tipo) {
            case QUEDA_PESO:
            case VACINA_PROX_VENC:
            case VACINA_VENC:
            case PERIODO_CARENCIA:
                throw new IllegalArgumentException(
                        "Essa regra é fixa do sistema e não pode ser configurada.");

            case PESAGEM_ATRASADA:
                if (parametro <= 0 || parametro != Math.floor(parametro)) {
                    throw new IllegalArgumentException(
                            "O prazo para pesagem deve ser um número inteiro de dias maior que zero.");
                }
                break;

            case CUSTO_ACIMA:
            case GMD_ABAIXO_META:
                if (parametro <= 0) {
                    throw new IllegalArgumentException(
                            "O parâmetro deve ser maior que zero.");
                }
                break;
        }
    }
}