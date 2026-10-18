package com.Pecucore.system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.VacinaRequestDTO;
import com.Pecucore.system.model.Vacina;
import com.Pecucore.system.repository.VacinaRepository;
import com.Pecucore.system.repository.VacinacaoRepository;
import java.util.List;
import java.util.Locale;

@Service
public class VacinaService {

    @Autowired
    private VacinaRepository vacinaRepository;

    @Autowired
    private VacinacaoRepository vacinacaoRepository;

    public Vacina create(VacinaRequestDTO dados) {
        String nome = normalizarNome(dados.nome());
        if (vacinaRepository.existsByNome(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma vacina com este nome.");
        }

        Vacina vacina = new Vacina();
        vacina.setNome(nome);
        vacina.setFabricante(dados.fabricante().trim());
        vacina.setCarenciaDias(dados.carenciaDias());
        vacina.setIntervaloDoseDias(dados.intervaloDoseDias());

        return vacinaRepository.save(vacina);
    }

    public Vacina update(Long id, VacinaRequestDTO dados) {
        Vacina vacinaExistente = getVacinaById(id);

        String nome = normalizarNome(dados.nome());
        if (vacinaRepository.existsByNomeAndIdNot(nome, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe outra vacina com este nome"); 
        }

        vacinaExistente.setNome(nome);
        vacinaExistente.setFabricante(dados.fabricante().trim());
        vacinaExistente.setCarenciaDias(dados.carenciaDias());
        vacinaExistente.setIntervaloDoseDias(dados.intervaloDoseDias());

        return vacinaRepository.save(vacinaExistente);
    }

    public void delete(Long id) {
        Vacina vacina = getVacinaById(id);

        if (vacinacaoRepository.existsByVacinaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível excluir uma vacina que já foi aplicada em algum animal");
        }

        vacinaRepository.delete(vacina);
    }

    public List<Vacina> getAllVacinas() {
        return vacinaRepository.findAll();
    }

    public Vacina getVacinaById(Long id) {
        return vacinaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vacina não encontrada"));
    }

    private String normalizarNome(String nome) {
        return nome.trim().toUpperCase(Locale.ROOT);
    }
}
