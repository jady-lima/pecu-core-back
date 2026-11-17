package com.pecucore.system.core.service.animal;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.repository.AnimalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimalConsultaService {

    @Autowired
    private AnimalRepository animalRepository;

    public long countAnimaisAtivosByLoteId(Long loteId) {
        return animalRepository.countByLoteIdAndStatus(loteId, StatusAnimal.ATIVO);
    }

    public long countAnimaisAtivosByPropriedadeId(Long propriedadeId) {
        return animalRepository.countByLotePropriedadeIdAndStatus(propriedadeId, StatusAnimal.ATIVO);
    }

    public List<Animal> getAnimaisAtivosByPropriedadeId(Long propriedadeId) {
        return animalRepository.findByLotePropriedadeIdAndStatusOrderByBrinco(propriedadeId, StatusAnimal.ATIVO);
    }

    public boolean existsAnimalAtivoByLoteId(Long loteId) {
        return animalRepository.existsByLoteIdAndStatus(loteId, StatusAnimal.ATIVO);
    }
}
