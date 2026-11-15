package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.exception.AcessoNegadoException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.*;

import com.pecucore.system.core.repository.AlertaRepository;
import com.pecucore.system.core.repository.CustoRepository;
import com.pecucore.system.core.repository.RegraAlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
        List<Custo> custos = custoRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (custos.isEmpty() || animal.getLote() == null || animal.getLote().getPropriedade() == null) {
            return;
        }

        RegraAlerta regra = regraAlertaRepository.findByPropriedadeIdAndTipoAndAtivaTrue(
                animal.getLote().getPropriedade().getId(),
                TipoAlerta.CUSTO_ACIMA
            ).stream().findFirst().orElse(null);

        if (regra == null) {
            return;
        }

        double custoTotal = custos.stream().mapToDouble(Custo::getValor).sum();

        if (custoTotal <= regra.getParametro()) {
            return;
        }

        if (alertaRepository.existsByAnimalIdAndTipoAndStatusIn(
            animal.getId(), 
            TipoAlerta.CUSTO_ACIMA,
            List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE)
        )) {
            return;
        }

        createAlerta(animal, TipoAlerta.CUSTO_ACIMA,
                "O animal está com custo acumulado de R$ " + custoTotal
                + ", acima do limite estabelecido de R$ "
                + regra.getParametro() + ".");
    }

    public List<Alerta> getAllAlertas() {
        Usuario usuario = usuarioAtual();

        return alertaRepository.findAll()
                .stream()
                .filter(alerta -> {
                    try {
                        validarAcessoAoAlerta(alerta, usuario);
                        return true;
                    } catch (AcessoNegadoException e) {
                        return false;
                    }
                })
                .toList();
    }

    public Alerta updateStatusAlerta(Long id, StatusAlerta novoStatus) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado"));

        validarAcessoAoAlerta(alerta, usuarioAtual());

        alerta.alterarStatus(novoStatus);
        return alertaRepository.save(alerta);
    }

    private Usuario usuarioAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Usuario)) {
            throw new AcessoNegadoException("Usuário não autenticado.");
        }

        return (Usuario) autenticacao.getPrincipal();
    }

    private boolean temAcesso(Propriedade propriedade, Usuario usuario) {
        if (usuario.getPerfil() == Perfil.ADMIN) {
            return true;
        }

        boolean produtorResponsavel = propriedade.getUsuario() != null && propriedade.getUsuario().getId().equals(usuario.getId());

        boolean veterinarioAssociado = propriedade.getVeterinarios().stream().anyMatch(v -> v.getId().equals(usuario.getId()));

        return produtorResponsavel || veterinarioAssociado;
    }

    private void validarAcessoAoAlerta(Alerta alerta, Usuario usuario) {
        if (alerta.getAnimal() == null
            || alerta.getAnimal().getLote() == null
            || alerta.getAnimal().getLote().getPropriedade() == null
            || !temAcesso(
            alerta.getAnimal().getLote().getPropriedade(), usuario)
        ) {
            throw new AcessoNegadoException("Você não tem acesso a este alerta.");
        }

        if (usuario.getPerfil() == Perfil.VETERINARIO && alerta.getTipo().isFinanceiro()) {
            throw new AcessoNegadoException("Você não tem permissão para acessar alertas financeiros.");
        }
    }

    public void createAlerta(Animal animal, TipoAlerta tipo, String mensagem) {
        alertaRepository.save(Alerta.abrir(animal, tipo, mensagem));
    }
}
