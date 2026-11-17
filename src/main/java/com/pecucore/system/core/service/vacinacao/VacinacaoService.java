package com.pecucore.system.core.service.vacinacao;

import com.pecucore.system.core.service.animal.AnimalService;
import com.pecucore.system.core.service.usuario.UsuarioService;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.*;
import com.pecucore.system.core.evento.VacinacaoRegistradaEvento;
import com.pecucore.system.core.service.custo.CustoAutomaticoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pecucore.system.core.dto.vacinacao.VacinacaoRequestDTO;
import com.pecucore.system.core.dto.vacinacao.VacinacaoUpdateRequestDTO;
import com.pecucore.system.core.repository.VacinacaoRepository;
import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitaria;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VacinacaoService {

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private VacinaService vacinaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PoliticaRestricaoSanitaria politicaRestricaoSanitaria;

    @Autowired
    private CustoAutomaticoService custoAutomaticoService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public Vacinacao createVacinacao(VacinacaoRequestDTO dados, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalAtivoByBrinco(dados.brinco(), usuarioLogado);
        animalService.validarDataRegistro(animal, dados.data());

        Vacina vacina = vacinaService.getVacinaById(dados.vacinaId());
        Usuario aplicador = usuarioService.getUsuarioById(dados.aplicadorId());
        Usuario registrador = usuarioService.getUsuarioById(usuarioLogado.getId());
        
        Vacinacao vacinacao = new Vacinacao();
        vacinacao.setAnimal(animal);
        vacinacao.setUsuarioRegistro(registrador);

        preencherDados(vacinacao, vacina, aplicador, dados.data(), dados.loteVacina(), dados.doseAplicada(), dados.observacoes(), dados.valor());

        Vacinacao salva = vacinacaoRepository.save(vacinacao);
        eventPublisher.publishEvent(new VacinacaoRegistradaEvento(salva.getAnimal().getId()));
        return salva;
    }

    @Transactional
    public Vacinacao updateVacinacao(Long id, VacinacaoUpdateRequestDTO dados, Usuario usuarioLogado) {
        Vacinacao vacinacao = getVacinacaoById(id, usuarioLogado);
        animalService.validarDataRegistro(vacinacao.getAnimal(), dados.data());

        Vacina vacina = vacinaService.getVacinaById(dados.vacinaId());
        Usuario aplicador = usuarioService.getUsuarioById(dados.aplicadorId());

        preencherDados(vacinacao, vacina, aplicador, dados.data(), dados.loteVacina(), dados.doseAplicada(), dados.observacoes(), dados.valor());

        Vacinacao salva = vacinacaoRepository.save(vacinacao);
        eventPublisher.publishEvent(new VacinacaoRegistradaEvento(salva.getAnimal().getId()));
        return salva;
    }

    @Transactional
    public void deleteVacinacao(Long id, Usuario usuarioLogado) {
        Vacinacao vacinacao = getVacinacaoById(id, usuarioLogado);
        Custo custo = vacinacao.getCusto();

        vacinacaoRepository.delete(vacinacao);

        if (custo != null) {
            custoAutomaticoService.deleteCustoAutomatico(custo);
        }
    }

    public Vacinacao getVacinacaoById(Long id, Usuario usuarioLogado) {
        Vacinacao vacinacao = vacinacaoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException( "Vacinação não encontrada"));
        animalService.getAnimalById(vacinacao.getAnimal().getId(), usuarioLogado);
        return vacinacao;
    }

    public List<Vacinacao> getHistorico(int brinco, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalByBrinco(brinco, usuarioLogado);
        return getHistoricoByAnimalId(animal.getId());
    }

    public List<Vacinacao> getHistoricoByAnimalId(Long animalId) {
        return vacinacaoRepository.findByAnimalIdOrderByDataAscIdAsc(animalId);
    }

    /**
     * Última vacinação de cada vacina aplicada no animal: é ela que define a próxima dose em vigor.
     */
    public List<Vacinacao> getUltimasVacinacoesByAnimalId(Long animalId) {
        Map<Long, Vacinacao> ultimaPorVacina = new LinkedHashMap<>();

        getHistoricoByAnimalId(animalId)
                .forEach(vacinacao -> ultimaPorVacina.put(vacinacao.getVacina().getId(), vacinacao));

        return List.copyOf(ultimaPorVacina.values());
    }

    private void preencherDados(Vacinacao vacinacao, Vacina vacina, Usuario aplicador, LocalDate data, String loteVacina, Double doseAplicada, String observacoes, Double valor) {
        vacinacao.setData(data);
        vacinacao.setVacina(vacina);
        vacinacao.setLoteVacina(loteVacina.trim());
        vacinacao.setDoseAplicada(doseAplicada);
        vacinacao.setObservacoes(observacoes == null || observacoes.isBlank() ? null : observacoes.trim());
        vacinacao.setUsuarioAplicador(aplicador);
        vacinacao.setValor(valor);
        vacinacao.setDataFimCarencia(politicaRestricaoSanitaria.calcularFimCarencia(data, vacina.getCarenciaDias()));
        vacinacao.setDataProximaDose(vacina.getIntervaloDoseDias() != null ? data.plusDays(vacina.getIntervaloDoseDias()) : null);

        vacinacao.setCusto(custoAutomaticoService.sincronizarCustoAutomatico(vacinacao.getCusto(), TipoCusto.VACINA, valor, data, vacinacao.getAnimal()));
    }
}
