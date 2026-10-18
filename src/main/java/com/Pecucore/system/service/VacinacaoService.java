package com.Pecucore.system.service;

import com.Pecucore.system.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.VacinacaoRequestDTO;
import com.Pecucore.system.dto.VacinacaoUpdateRequestDTO;
import com.Pecucore.system.repository.VacinacaoRepository;
import com.Pecucore.system.service.calculo.CalculadoraCarencia;
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
    private CustoService custoService;

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
            custoService.excluirAutomatico(custo);
        }
    }

    public Vacinacao getVacinacaoById(Long id, Usuario usuarioLogado) {
        Vacinacao vacinacao = vacinacaoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vacinação não encontrada"));
        animalService.getAnimalById(vacinacao.getAnimal().getId(), usuarioLogado);
        return vacinacao;
    }

    public List<Vacinacao> getHistorico(int brinco, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalByBrinco(brinco, usuarioLogado);
        return vacinacaoRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());
    }

    private void preencherDados(Vacinacao vacinacao, Vacina vacina, Usuario aplicador, LocalDate data, String loteVacina, Double doseAplicada, String observacoes, Double valor) {
        vacinacao.setData(data);
        vacinacao.setVacina(vacina);
        vacinacao.setLoteVacina(loteVacina.trim());
        vacinacao.setDoseAplicada(doseAplicada);
        vacinacao.setObservacoes(observacoes == null || observacoes.isBlank() ? null : observacoes.trim());
        vacinacao.setUsuarioAplicador(aplicador);
        vacinacao.setValor(valor);
        vacinacao.setDataFimCarencia(calculadoraCarencia.calculateDataFimCarencia(data, vacina.getCarenciaDias()));
        vacinacao.setDataProximaDose(vacina.getIntervaloDoseDias() != null ? data.plusDays(vacina.getIntervaloDoseDias()) : null);

        vacinacao.setCusto(custoService.sincronizarAutomatico(vacinacao.getCusto(), TipoCusto.VACINA, valor, data, vacinacao.getAnimal()));
    }
}
