package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.exception.AcessoNegadoException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.*;

import com.pecucore.system.core.repository.AlertaRepository;
import com.pecucore.system.core.repository.CustoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@Service
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private RegraAlertaService regraAlertaService;

    @Autowired
    private CustoRepository custoRepository;

    public void verificarCustoAcima(Animal animal) {
        Optional<Double> limite = regraAlertaService.getParametroAtivo(animal, TipoAlerta.CUSTO_ACIMA);

        if (limite.isEmpty()) {
            return;
        }

        double custoTotal = custoRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId()).stream()
                .mapToDouble(Custo::getValor)
                .sum();

        if (custoTotal <= limite.get()) {
            return;
        }

        createAlertaSeNaoExistir(animal, TipoAlerta.CUSTO_ACIMA,
                "O animal está com custo acumulado de R$ " + custoTotal + ", acima do limite estabelecido de R$ " + limite.get() + ".");
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
        Alerta alerta = alertaRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado"));

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

    public boolean createAlertaSeNaoExistir(Animal animal, TipoAlerta tipo, String mensagem) {
        if (alertaRepository.existsByAnimalIdAndTipoAndStatusIn(animal.getId(), tipo, StatusAlerta.ativos())) {
            return false;
        }

        alertaRepository.save(Alerta.abrir(animal, tipo, mensagem));
        return true;
    }
}
