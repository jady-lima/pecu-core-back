package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.extensao.alerta.CatalogoTiposAlerta;
import com.pecucore.system.core.extensao.alerta.TipoAlerta;
import com.pecucore.system.core.dto.alerta.AlertaStatusRequestDTO;
import com.pecucore.system.core.exception.AcessoNegadoException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.*;
import com.pecucore.system.core.repository.AlertaRepository;
import com.pecucore.system.core.service.propriedade.AcessoPropriedadeService;
import com.pecucore.system.core.service.animal.AnimalService;
import com.pecucore.system.core.service.politica.PoliticaVisibilidadeFinanceira;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private AcessoPropriedadeService acessoPropriedadeService;

    @Autowired
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira;

    @Autowired
    private CatalogoTiposAlerta catalogoTiposAlerta;

    @Autowired
    private AnimalService animalService;

    public List<TipoAlerta> getAllTiposAlerta(Usuario usuarioLogado) {
        return catalogoTiposAlerta.getAllTiposAlerta().stream()
                .filter(tipo -> politicaVisibilidadeFinanceira.podeVerAlerta(tipo, usuarioLogado))
                .toList();
    }

    public List<Alerta> getAllAlertas(Usuario usuarioLogado, Long animalId, StatusAlerta status) {
        List<Alerta> alertas = animalId != null
                ? getAlertasDoAnimal(animalId, usuarioLogado)
                : acessoPropriedadeService.filtrarPorPerfil(
                        usuarioLogado,
                        alertaRepository::findAllByOrderByDataCriacaoDesc,
                        alertaRepository::findByAnimalLotePropriedadeUsuarioIdOrderByDataCriacaoDesc,
                        alertaRepository::findByAnimalLotePropriedadeVeterinariosIdOrderByDataCriacaoDesc
                );

        return alertas.stream()
                .filter(alerta -> status == null || alerta.getStatus() == status)
                .filter(alerta -> podeVer(alerta, usuarioLogado))
                .toList();
    }

    public List<Alerta> getHistoricoByAnimalId(Long animalId, Usuario usuarioLogado) {
        return alertaRepository.findByAnimalIdOrderByDataCriacaoDesc(animalId).stream()
                .filter(alerta -> podeVer(alerta, usuarioLogado))
                .toList();
    }

    public List<Alerta> getAlertasAtivosByLoteId(Long loteId, Usuario usuarioLogado) {
        return alertaRepository.findByAnimalLoteIdAndAnimalStatusAndStatusIn(loteId, StatusAnimal.ATIVO, StatusAlerta.ativos()).stream()
                .filter(alerta -> podeVer(alerta, usuarioLogado))
                .toList();
    }

    @Transactional
    public Alerta updateStatusAlerta(Long id, AlertaStatusRequestDTO dados, Usuario usuarioLogado) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado"));

        validarAcessoAoAlerta(alerta, usuarioLogado);

        alerta.alterarStatus(dados.status());
        return alertaRepository.save(alerta);
    }

    private List<Alerta> getAlertasDoAnimal(Long animalId, Usuario usuarioLogado) {
        animalService.getAnimalById(animalId, usuarioLogado);
        return alertaRepository.findByAnimalIdOrderByDataCriacaoDesc(animalId);
    }

    private void validarAcessoAoAlerta(Alerta alerta, Usuario usuarioLogado) {
        acessoPropriedadeService.validarAcesso(
                propriedadeDoAlerta(alerta),
                usuarioLogado,
                "Você não tem acesso a este alerta."
        );

        if (!podeVer(alerta, usuarioLogado)) {
            throw new AcessoNegadoException("Você não tem permissão para acessar alertas financeiros.");
        }
    }

    private boolean podeVer(Alerta alerta, Usuario usuarioLogado) {
        return politicaVisibilidadeFinanceira.podeVerAlerta(alerta.getTipo(), usuarioLogado);
    }

    private Propriedade propriedadeDoAlerta(Alerta alerta) {
        Lote lote = alerta.getAnimal().getLote();
        return lote != null ? lote.getPropriedade() : null;
    }
}
