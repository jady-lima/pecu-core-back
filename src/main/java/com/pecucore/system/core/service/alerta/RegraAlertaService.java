package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.dto.alerta.RegraAlertaRequestDTO;
import com.pecucore.system.core.dto.alerta.RegraAlertaUpdateRequestDTO;
import com.pecucore.system.core.exception.AcessoNegadoException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Propriedade;
import com.pecucore.system.core.model.RegraAlerta;
import com.pecucore.system.core.extensao.alerta.CatalogoTiposAlerta;
import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.RegraAlertaRepository;
import com.pecucore.system.core.service.propriedade.AcessoPropriedadeService;
import com.pecucore.system.core.service.propriedade.PropriedadeService;
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

    @Autowired
    private CatalogoTiposAlerta catalogoTiposAlerta;

    @Transactional
    public RegraAlerta createRegraAlerta(RegraAlertaRequestDTO dados, Usuario usuarioLogado) {
        Propriedade propriedade = propriedadeService.getPropriedadeById(dados.propriedadeId(), usuarioLogado);
        TipoAlerta tipo = getTipoAlerta(dados.tipo());

        validarPermissaoFinanceira(tipo, usuarioLogado);

        if (regraAlertaRepository.existsByPropriedadeIdAndTipo(propriedade.getId(), tipo)) {
            throw new RegraNegocioException("Já existe uma regra de " + tipo.getCodigo()
                    + " para esta propriedade. Atualize a regra existente.");
        }

        return regraAlertaRepository.save(RegraAlerta.criar(propriedade, tipo, dados.parametro()));
    }

    @Transactional
    public RegraAlerta updateRegraAlerta(Long id, RegraAlertaUpdateRequestDTO dados, Usuario usuarioLogado) {
        RegraAlerta regra = regraAlertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Regra de alerta não encontrada"));

        acessoPropriedadeService.validarAcesso(
                regra.getPropriedade(),
                usuarioLogado,
                "Você não tem acesso a esta propriedade"
        );
        validarPermissaoFinanceira(regra.getTipo(), usuarioLogado);

        regra.atualizar(dados.parametro(), dados.ativa());

        return regraAlertaRepository.save(regra);
    }

    public List<RegraAlerta> getAllRegrasAlerta(Long propriedadeId, Usuario usuarioLogado) {
        Propriedade propriedade = propriedadeService.getPropriedadeById(propriedadeId, usuarioLogado);

        return regraAlertaRepository.findByPropriedadeIdOrderByTipo(propriedade.getId()).stream()
                .filter(regra -> politicaVisibilidadeFinanceira.podeVerAlerta(regra.getTipo(), usuarioLogado))
                .toList();
    }

    public Optional<Double> getParametroAtivo(Animal animal, TipoAlerta tipo) {
        List<RegraAlerta> regras = getRegrasDoAnimal(animal, tipo);

        if (regras.isEmpty()) {
            return tipo.getParametroPadrao();
        }

        return regras.stream()
                .filter(RegraAlerta::isAtiva)
                .map(RegraAlerta::getParametro)
                .findFirst();
    }

    public boolean isAlertaDesativado(Animal animal, TipoAlerta tipo) {
        List<RegraAlerta> regras = getRegrasDoAnimal(animal, tipo);
        return !regras.isEmpty() && regras.stream().noneMatch(RegraAlerta::isAtiva);
    }

    private List<RegraAlerta> getRegrasDoAnimal(Animal animal, TipoAlerta tipo) {
        if (animal.getLote() == null || animal.getLote().getPropriedade() == null) {
            return List.of();
        }

        return regraAlertaRepository.findByPropriedadeIdAndTipo(animal.getLote().getPropriedade().getId(), tipo);
    }

    private TipoAlerta getTipoAlerta(String codigo) {
        return catalogoTiposAlerta.getTipoAlertaByCodigo(codigo)
                .orElseThrow(() -> new RegraNegocioException(
                        "Tipo de alerta inválido: " + codigo + ". Consulte os tipos aceitos em GET /alertas/tipos."
                ));
    }

    private void validarPermissaoFinanceira(TipoAlerta tipo, Usuario usuarioLogado) {
        if (tipo.isFinanceiro() && !politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuarioLogado)) {
            throw new AcessoNegadoException("Você não tem permissão para gerenciar regras financeiras.");
        }
    }
}
