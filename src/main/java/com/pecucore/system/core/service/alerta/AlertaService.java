package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.exception.AcessoNegadoException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.*;
import com.pecucore.system.core.repository.AlertaRepository;
import com.pecucore.system.core.repository.CustoRepository;
import com.pecucore.system.core.service.AcessoPropriedadeService;
import com.pecucore.system.core.service.politica.PoliticaVisibilidadeFinanceira;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private AcessoPropriedadeService acessoPropriedadeService;

    @Autowired
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira;

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

    public List<Alerta> getAllAlertas(Usuario usuarioLogado) {
        return acessoPropriedadeService.filtrarPorPerfil(
                        usuarioLogado,
                        alertaRepository::findAllByOrderByDataCriacaoDesc,
                        alertaRepository::findByAnimalLotePropriedadeUsuarioIdOrderByDataCriacaoDesc,
                        alertaRepository::findByAnimalLotePropriedadeVeterinariosIdOrderByDataCriacaoDesc
                )
                .stream()
                .filter(alerta -> politicaVisibilidadeFinanceira.podeVerAlerta(alerta.getTipo(), usuarioLogado))
                .toList();
    }

    @Transactional
    public Alerta updateStatusAlerta(Long id, StatusAlerta novoStatus, Usuario usuarioLogado) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado"));

        validarAcessoAoAlerta(alerta, usuarioLogado);

        alerta.alterarStatus(novoStatus);
        return alertaRepository.save(alerta);
    }

    @Transactional
    public boolean createAlertaSeNaoExistir(Animal animal, TipoAlerta tipo, String mensagem) {
        if (alertaRepository.existsByAnimalIdAndTipoAndStatusIn(animal.getId(), tipo, StatusAlerta.ativos())) {
            return false;
        }

        alertaRepository.save(Alerta.abrir(animal, tipo, mensagem));
        return true;
    }

    private void validarAcessoAoAlerta(Alerta alerta, Usuario usuarioLogado) {
        acessoPropriedadeService.validarAcesso(
                propriedadeDoAlerta(alerta),
                usuarioLogado,
                "Você não tem acesso a este alerta."
        );

        if (!politicaVisibilidadeFinanceira.podeVerAlerta(alerta.getTipo(), usuarioLogado)) {
            throw new AcessoNegadoException("Você não tem permissão para acessar alertas financeiros.");
        }
    }

    private Propriedade propriedadeDoAlerta(Alerta alerta) {
        Lote lote = alerta.getAnimal().getLote();
        return lote != null ? lote.getPropriedade() : null;
    }
}
