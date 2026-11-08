package com.pecucore.system.service.alerta;

import com.pecucore.system.exception.RecursoNaoEncontradoException;
import com.pecucore.system.model.*;

import com.pecucore.system.repository.AlertaRepository;
import com.pecucore.system.repository.CustoRepository;
import com.pecucore.system.repository.RegraAlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private RegraAlertaRepository regraAlertaRepository;

    @Autowired
    private CustoRepository custoRepository;

    public void verificarCustoAcima(Animal animal) {
        List<Custo> custos = custoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (custos.isEmpty() || animal.getLote() == null
                || animal.getLote().getPropriedade() == null) {
            return;
        }

        RegraAlerta regra = regraAlertaRepository
                .findByPropriedadeIdAndTipoAndAtivaTrue(
                        animal.getLote().getPropriedade().getId(),
                        TipoAlerta.CUSTO_ACIMA)
                .stream().findFirst().orElse(null);

        if (regra == null) {
            return;
        }

        double custoTotal = custos.stream()
                .mapToDouble(Custo::getValor).sum();

        if (custoTotal <= regra.getParametro()) {
            return;
        }

        if (alertaRepository.existsByAnimalIdAndTipoAndStatusIn(
                animal.getId(), TipoAlerta.CUSTO_ACIMA,
                List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE))) {
            return;
        }

        criarAlerta(animal, TipoAlerta.CUSTO_ACIMA,
                "O animal está com custo acumulado de R$ " + custoTotal
                        + ", acima do limite estabelecido de R$ "
                        + regra.getParametro() + ".");
    }

    public List<Alerta> listarAlertas() {
        Usuario usuario = usuarioAtual();

        return alertaRepository.findAll().stream()
                .filter(alerta -> {
                    try {
                        validarAcessoAoAlerta(alerta, usuario);
                        return true;
                    } catch (AccessDeniedException e) {
                        return false;
                    }
                })
                .toList();
    }

    public Alerta atualizarStatus(Long id, StatusAlerta novoStatus) {
        if (novoStatus == null) {
            throw new IllegalArgumentException("O status é obrigatório.");
        }

        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Alerta não encontrado"
                ));

        validarAcessoAoAlerta(alerta, usuarioAtual());

        alerta.setStatus(novoStatus);
        return alertaRepository.save(alerta);
    }

    private Usuario usuarioAtual() {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null
                || !(autenticacao.getPrincipal() instanceof Usuario)) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        return (Usuario) autenticacao.getPrincipal();
    }

    private boolean temAcesso(Propriedade propriedade, Usuario usuario) {
        if (usuario.getPerfil() == Perfil.ADMIN) {
            return true;
        }

        boolean produtorResponsavel = propriedade.getUsuario() != null
                && propriedade.getUsuario().getId().equals(usuario.getId());

        boolean veterinarioAssociado = propriedade.getVeterinarios().stream()
                .anyMatch(v -> v.getId().equals(usuario.getId()));

        return produtorResponsavel || veterinarioAssociado;
    }

    private void validarAcessoAoAlerta(Alerta alerta, Usuario usuario) {
        if (alerta.getAnimal() == null
                || alerta.getAnimal().getLote() == null
                || alerta.getAnimal().getLote().getPropriedade() == null
                || !temAcesso(
                alerta.getAnimal().getLote().getPropriedade(), usuario)) {
            throw new AccessDeniedException(
                    "Você não tem acesso a este alerta.");
        }

        if (usuario.getPerfil() == Perfil.VETERINARIO
                && alerta.getTipo() == TipoAlerta.CUSTO_ACIMA) {
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar alertas financeiros.");
        }
    }

    private SeveridadeAlerta definirSeveridade(TipoAlerta tipo) {
        switch (tipo) {
            case QUEDA_PESO:
            case VACINA_VENC:
            case GMD_ABAIXO_META:
            case CUSTO_ACIMA:
                return SeveridadeAlerta.ALTA;

            case VACINA_PROX_VENC:
                return SeveridadeAlerta.BAIXA;

            case PERIODO_CARENCIA:
            case PESAGEM_ATRASADA:
            default:
                return SeveridadeAlerta.MEDIA;
        }
    }

    public void criarAlerta(
            Animal animal,
            TipoAlerta tipo,
            String mensagem
    ) {
        Alerta alerta = new Alerta();
        alerta.setAnimal(animal);
        alerta.setTipo(tipo);
        alerta.setSeveridade(definirSeveridade(tipo));
        alerta.setMensagem(mensagem);
        alerta.setStatus(StatusAlerta.ABERTO);
        alerta.setDataCriacao(LocalDateTime.now());

        alertaRepository.save(alerta);
    }
}