package com.Pecucore.system.service;

import com.Pecucore.system.repository.PropriedadeRepository;
import com.Pecucore.system.model.Propriedade;
import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class PropriedadeService {

    private final PropriedadeRepository propriedadeRepository;
    public PropriedadeService(PropriedadeRepository propriedadeRepository) {
        this.propriedadeRepository = propriedadeRepository;
    }
    public Propriedade create(Propriedade propriedade) {
        return propriedadeRepository.save(propriedade);
}
    public List<Propriedade> findAll() {
        return propriedadeRepository.findAll();
    }
    public Propriedade findById(Long id) {
        return propriedadeRepository.findById(id).orElse(null);
        }
    public Propriedade update(Long id, Propriedade novaPropriedade) {

        Propriedade propriedade = propriedadeRepository.findById(id).orElse(null);

        if (propriedade == null) {
            return null;
        }

        propriedade.setNome(novaPropriedade.getNome());
        propriedade.setLocalizacao(novaPropriedade.getLocalizacao());

        return propriedadeRepository.save(propriedade);
    }
    public void delete(Long id) {
        propriedadeRepository.deleteById(id);
    }
}

