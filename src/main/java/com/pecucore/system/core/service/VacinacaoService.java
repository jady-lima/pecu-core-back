package com.pecucore.system.core.service;

import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.*;
import com.pecucore.system.core.service.custo.CustoAutomaticoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pecucore.system.core.dto.vacinacao.VacinacaoRequestDTO;
import com.pecucore.system.core.dto.vacinacao.VacinacaoUpdateRequestDTO;
import com.pecucore.system.core.repository.VacinacaoRepository;
import com.pecucore.system.bovinos.sanidade.CalculadoraCarencia;
import java.time.LocalDate;
import java.util.List;

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
    private CalculadoraCarencia calculadoraCarencia;

    @Autowired
    private CustoAutomaticoService custoAutomaticoService;

    @Transactional
    public Vacinacao create(VacinacaoRequestDTO dados, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalAtivoByBrinco(dados.brinco(), usuarioLogado);
        animalService.validarDataRegistro(animal, dados.data());

        Vacina vacina = vacinaService.getVacinaById(dados.vacinaId());
        Usuario aplicador = usuarioService.getUsuarioById(dados.aplicadorId());
        Usuario registrador = usuarioService.getUsuarioById(usuarioLogado.getId());
        
        Vacinacao vacinacao = new Vacinacao();
        vacinacao.setAnimal(animal);
        vacinacao.setUsuarioRegistro(registrador);

        preencherDados(vacinacao, vacina, aplicador, dados.data(), dados.loteVacina(), dados.doseAplicada(), dados.observacoes(), dados.valor());

        return vacinacaoRepository.save(vacinacao);
    }

    @Transactional
    public Vacinacao update(Long id, VacinacaoUpdateRequestDTO dados, Usuario usuarioLogado) {
        Vacinacao vacinacao = getVacinacaoById(id, usuarioLogado);
        animalService.validarDataRegistro(vacinacao.getAnimal(), dados.data());

        Vacina vacina = vacinaService.getVacinaById(dados.vacinaId());
        Usuario aplicador = usuarioService.getUsuarioById(dados.aplicadorId());

        preencherDados(vacinacao, vacina, aplicador, dados.data(), dados.loteVacina(), dados.doseAplicada(), dados.observacoes(), dados.valor());

        return vacinacaoRepository.save(vacinacao);
    }

    @Transactional
    public void delete(Long id, Usuario usuarioLogado) {
        Vacinacao vacinacao = getVacinacaoById(id, usuarioLogado);
        Custo custo = vacinacao.getCusto();

        vacinacaoRepository.delete(vacinacao);

        if (custo != null) {
            custoAutomaticoService.excluirAutomatico(custo);
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

    private void preencherDados(Vacinacao vacinacao, Vacina vacina, Usuario aplicador, LocalDate data, String loteVacina, Double doseAplicada, String observacoes, Double valor) {
        vacinacao.setData(data);
        vacinacao.setVacina(vacina);
        vacinacao.setLoteVacina(loteVacina.trim());
        vacinacao.setDoseAplicada(doseAplicada);
        vacinacao.setObservacoes(observacoes == null || observacoes.isBlank() ? null : observacoes.trim());
        vacinacao.setUsuarioAplicador(aplicador);
        vacinacao.setValor(valor);
        vacinacao.setDataFimCarencia(calculadoraCarencia.calcularDataFimCarencia(data, vacina.getCarenciaDias()));
        vacinacao.setDataProximaDose(vacina.getIntervaloDoseDias() != null ? data.plusDays(vacina.getIntervaloDoseDias()) : null);

        vacinacao.setCusto(custoAutomaticoService.sincronizarAutomatico(vacinacao.getCusto(), TipoCusto.VACINA, valor, data, vacinacao.getAnimal()));
    }
}
