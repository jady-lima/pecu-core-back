package com.pecucore.system.core.service;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pecucore.system.core.dto.vacinacao.VacinaRequestDTO;
import com.pecucore.system.core.model.Vacina;
import com.pecucore.system.core.repository.VacinaRepository;
import com.pecucore.system.core.repository.VacinacaoRepository;
import java.util.List;
import java.util.Locale;

@Service
public class VacinaService {

    @Autowired
    private VacinaRepository vacinaRepository;

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    public Vacina createVacina(VacinaRequestDTO dados) {
        String nome = normalizarNome(dados.nome());
        if (vacinaRepository.existsByNome(nome)) {
            throw new ConflitoNegocioException( "Já existe uma vacina com este nome.");
        }

        Vacina vacina = new Vacina();
        vacina.setNome(nome);
        vacina.setFabricante(dados.fabricante().trim());
        vacina.setCarenciaDias(dados.carenciaDias());
        vacina.setIntervaloDoseDias(dados.intervaloDoseDias());

        return vacinaRepository.save(vacina);
    }

    public Vacina updateVacina(Long id, VacinaRequestDTO dados) {
        Vacina vacinaExistente = getVacinaById(id);

        String nome = normalizarNome(dados.nome());
        if (vacinaRepository.existsByNomeAndIdNot(nome, id)) {
            throw new ConflitoNegocioException( "Já existe outra vacina com este nome");
        }

        vacinaExistente.setNome(nome);
        vacinaExistente.setFabricante(dados.fabricante().trim());
        vacinaExistente.setCarenciaDias(dados.carenciaDias());
        vacinaExistente.setIntervaloDoseDias(dados.intervaloDoseDias());

        return vacinaRepository.save(vacinaExistente);
    }

    public void deleteVacina(Long id) {
        Vacina vacina = getVacinaById(id);

        if (vacinacaoRepository.existsByVacinaId(id)) {
            throw new ConflitoNegocioException( "Não é possível excluir uma vacina que já foi aplicada em algum animal");
        }

        vacinaRepository.delete(vacina);
    }

    public List<Vacina> getAllVacinas() {
        return vacinaRepository.findAll();
    }

    public Vacina getVacinaById(Long id) {
        return vacinaRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException( "Vacina não encontrada"));
    }

    private String normalizarNome(String nome) {
        return nome.trim().toUpperCase(Locale.ROOT);
    }
}
