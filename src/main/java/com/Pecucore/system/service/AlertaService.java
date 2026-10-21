package com.Pecucore.system.service;

import com.Pecucore.system.model.*;
import com.Pecucore.system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private RegraAlertaRepository regraAlertaRepository;

    @Autowired
    private PesagemRepository pesagemRepository;

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    @Autowired
    private CustoRepository custoRepository;

    public void verificarGmdAbaixoMeta(Animal animal, Double gmd) {
        if (gmd == null || animal.getLote() == null
                || animal.getLote().getPropriedade() == null) {
            return;
        }

        Propriedade propriedade = animal.getLote().getPropriedade();

        RegraAlerta regra = regraAlertaRepository
                .findByPropriedadeIdAndTipoAndAtivaTrue(
                        propriedade.getId(), TipoAlerta.GMD_ABAIXO_META)
                .stream().findFirst().orElse(null);

        if (regra == null || gmd >= regra.getParametro()) {
            return;
        }

        if (alertaRepository.existsByAnimalIdAndTipoAndStatusIn(
                animal.getId(), TipoAlerta.GMD_ABAIXO_META,
                List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE))) {
            return;
        }

        criarAlerta(animal, TipoAlerta.GMD_ABAIXO_META,
                "GMD do animal abaixo da meta estabelecida: " + gmd + " kg/dia.");
    }

    public void verificarQuedaPeso(Animal animal) {
        List<Pesagem> pesagens = pesagemRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (pesagens.size() < 3) {
            return;
        }

        Pesagem ultima = pesagens.get(pesagens.size() - 1);
        Pesagem anterior = pesagens.get(pesagens.size() - 2);
        Pesagem anteriorAnterior = pesagens.get(pesagens.size() - 3);

        if (anterior.getPesoKg() >= anteriorAnterior.getPesoKg()
                || ultima.getPesoKg() >= anterior.getPesoKg()) {
            return;
        }

        if (alertaRepository.existsByAnimalIdAndTipoAndStatusIn(
                animal.getId(), TipoAlerta.QUEDA_PESO,
                List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE))) {
            return;
        }

        criarAlerta(animal, TipoAlerta.QUEDA_PESO,
                "Animal apresentou queda de peso em duas pesagens consecutivas.");
    }

    public void verificarPesagemAtrasada(Animal animal) {
        List<Pesagem> pesagens = pesagemRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        if (pesagens.isEmpty() || animal.getLote() == null
                || animal.getLote().getPropriedade() == null) {
            return;
        }

        Pesagem ultimaPesagem = pesagens.get(pesagens.size() - 1);

        RegraAlerta regra = regraAlertaRepository
                .findByPropriedadeIdAndTipoAndAtivaTrue(
                        animal.getLote().getPropriedade().getId(),
                        TipoAlerta.PESAGEM_ATRASADA)
                .stream().findFirst().orElse(null);

        if (regra == null) {
            return;
        }

        long diasSemPesagem = ChronoUnit.DAYS.between(
                ultimaPesagem.getData(), LocalDate.now());

        if (diasSemPesagem <= regra.getParametro()) {
            return;
        }

        if (alertaRepository.existsByAnimalIdAndTipoAndStatusIn(
                animal.getId(), TipoAlerta.PESAGEM_ATRASADA,
                List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE))) {
            return;
        }

        criarAlerta(animal, TipoAlerta.PESAGEM_ATRASADA,
                "Pesagem atrasada: animal está há " + diasSemPesagem
                        + " dias sem nova pesagem.");
    }

    public void verificarVacinaProximaVencimento(Animal animal) {
        List<Vacinacao> vacinacoes = vacinacaoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        LocalDate hoje = LocalDate.now();

        for (Vacinacao vacinacao : vacinacoes) {
            LocalDate proximaDose = vacinacao.getDataProximaDose();

            if (proximaDose == null) {
                continue;
            }

            long dias = ChronoUnit.DAYS.between(hoje, proximaDose);

            if (dias != 15 && dias != 7 && dias != 1) {
                continue;
            }

            String identificador = "Antecedência: " + dias
                    + " dia(s); dose: " + proximaDose;

            boolean existe = alertaRepository.findByAnimalIdOrderByDataCriacaoDesc(
                            animal.getId()).stream()
                    .anyMatch(a -> a.getTipo() == TipoAlerta.VACINA_PROX_VENC
                            && a.getMensagem() != null
                            && a.getMensagem().contains(identificador));

            if (existe) {
                continue;
            }

            criarAlerta(animal, TipoAlerta.VACINA_PROX_VENC,
                    "Atenção: vacina com dose prevista para "
                            + proximaDose + ". " + identificador);
        }
    }

    public void verificarVacinaVencida(Animal animal) {
        List<Vacinacao> vacinacoes = vacinacaoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        LocalDate hoje = LocalDate.now();

        for (Vacinacao vacinacao : vacinacoes) {
            LocalDate proximaDose = vacinacao.getDataProximaDose();

            if (proximaDose == null || !proximaDose.isBefore(hoje)) {
                continue;
            }

            String identificador =
                    "próxima dose estava prevista para " + proximaDose;

            boolean existe = alertaRepository.findByAnimalIdOrderByDataCriacaoDesc(
                            animal.getId()).stream()
                    .anyMatch(a -> a.getTipo() == TipoAlerta.VACINA_VENC
                            && a.getMensagem() != null
                            && a.getMensagem().contains(identificador));

            if (existe) {
                continue;
            }

            criarAlerta(animal, TipoAlerta.VACINA_VENC,
                    "Vacina vencida. A próxima dose estava prevista para "
                            + proximaDose + ".");
        }
    }

    public void verificarPeriodoCarencia(Animal animal) {
        List<Vacinacao> vacinacoes = vacinacaoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());

        LocalDate hoje = LocalDate.now();

        for (Vacinacao vacinacao : vacinacoes) {
            LocalDate fimCarencia = vacinacao.getDataFimCarencia();

            if (fimCarencia == null || !fimCarencia.isAfter(hoje)) {
                continue;
            }

            if (alertaRepository.existsByAnimalIdAndTipoAndStatusIn(
                    animal.getId(), TipoAlerta.PERIODO_CARENCIA,
                    List.of(StatusAlerta.ABERTO, StatusAlerta.EM_ANALISE))) {
                return;
            }

            criarAlerta(animal, TipoAlerta.PERIODO_CARENCIA,
                    "Animal em período de carência até " + fimCarencia
                            + ". Não deve ser destinado à venda ou ao abate.");
            return;
        }
    }

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
                .orElseThrow(() -> new RuntimeException("Alerta não encontrado"));

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

    private void criarAlerta(
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