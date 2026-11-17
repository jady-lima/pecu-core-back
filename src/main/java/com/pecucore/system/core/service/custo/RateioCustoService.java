package com.pecucore.system.core.service.custo;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Custo;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.model.TipoCusto;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.CustoRepository;
import com.pecucore.system.core.service.animal.AnimalConsultaService;
import com.pecucore.system.core.service.animal.AnimalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RateioCustoService {

    @Autowired
    private CustoRepository custoRepository;

    @Autowired
    private AnimalConsultaService animalConsultaService;

    @Autowired
    private AnimalService animalService;

    public RateioCustoAnimal getRateioCustoByAnimalId(Long animalId, Usuario usuarioLogado) {
        return calcularRateio(animalService.getAnimalById(animalId, usuarioLogado));
    }

    public RateioCustoAnimal calcularRateio(Animal animal) {
        Map<TipoCusto, Double> totalPorTipo = new EnumMap<>(TipoCusto.class);

        double direto = somar(custoRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId()), 1, totalPorTipo);
        double rateadoLote = 0;
        double rateadoPropriedade = 0;

        if (participaDoRateio(animal)) {
            Long loteId = animal.getLote().getId();
            Long propriedadeId = animal.getLote().getPropriedade().getId();

            rateadoLote = somar(
                    custoRepository.findByLoteId(loteId),
                    animalConsultaService.countAnimaisAtivosByLoteId(loteId),
                    totalPorTipo
            );
            rateadoPropriedade = somar(
                    custoRepository.findByPropriedadeId(propriedadeId),
                    animalConsultaService.countAnimaisAtivosByPropriedadeId(propriedadeId),
                    totalPorTipo
            );
        }

        return new RateioCustoAnimal(animal, direto, rateadoLote, rateadoPropriedade, totalPorTipo);
    }

    public double calcularCustoMedio(List<Animal> animais) {
        if (animais.isEmpty()) {
            return 0;
        }

        List<Long> animalIds = animais.stream().map(Animal::getId).toList();
        double total = custoRepository.findByAnimalIdIn(animalIds).stream()
                .mapToDouble(Custo::getValor)
                .sum();

        Map<Long, Double> cotaPorLote = new HashMap<>();
        Map<Long, Double> cotaPorPropriedade = new HashMap<>();

        for (Animal animal : animais) {
            if (!participaDoRateio(animal)) {
                continue;
            }

            total += cotaPorLote.computeIfAbsent(animal.getLote().getId(), this::calcularCotaLote);
            total += cotaPorPropriedade.computeIfAbsent(animal.getLote().getPropriedade().getId(), this::calcularCotaPropriedade);
        }

        return total / animais.size();
    }

    private double calcularCotaLote(Long loteId) {
        return somar(custoRepository.findByLoteId(loteId), animalConsultaService.countAnimaisAtivosByLoteId(loteId), null);
    }

    private double calcularCotaPropriedade(Long propriedadeId) {
        return somar(
                custoRepository.findByPropriedadeId(propriedadeId),
                animalConsultaService.countAnimaisAtivosByPropriedadeId(propriedadeId),
                null
        );
    }

    private boolean participaDoRateio(Animal animal) {
        return animal.getStatus() == StatusAnimal.ATIVO
                && animal.getLote() != null
                && animal.getLote().getPropriedade() != null;
    }

    private double somar(List<Custo> custos, long divisor, Map<TipoCusto, Double> totalPorTipo) {
        if (divisor <= 0) {
            return 0;
        }

        double soma = 0;

        for (Custo custo : custos) {
            double parte = custo.getValor() / divisor;
            soma += parte;

            if (totalPorTipo != null) {
                totalPorTipo.merge(custo.getTipo(), parte, Double::sum);
            }
        }

        return soma;
    }
}
