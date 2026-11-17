package com.pecucore.system.core.service.animal;

import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.repository.AnimalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnimalConsultaService {

    @Autowired
    private AnimalRepository animalRepository;

    public long countAnimaisAtivosByLoteId(Long loteId) {
        return animalRepository.countByLoteIdAndStatus(loteId, StatusAnimal.ATIVO);
    }

    public boolean existsAnimalAtivoByLoteId(Long loteId) {
        return animalRepository.existsByLoteIdAndStatus(loteId, StatusAnimal.ATIVO);
    }
}
