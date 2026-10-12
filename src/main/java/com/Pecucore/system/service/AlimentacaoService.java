package com.Pecucore.system.service;

import com.Pecucore.system.model.TipoCusto;
import com.Pecucore.system.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.AlimentacaoLoteRequestDTO;
import com.Pecucore.system.dto.AlimentacaoRequestDTO;
import com.Pecucore.system.model.Alimentacao;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.TipoAlimentacao;
import com.Pecucore.system.repository.AlimentacaoRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class AlimentacaoService {

    @Autowired
    private AlimentacaoRepository alimentacaoRepository;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private LoteService loteService;

    @Autowired
    private CustoService custoService;

    @Transactional
    public Alimentacao create(AlimentacaoRequestDTO dados) {

        Animal animal =
                animalService.getAnimalAtivoByBrinco(
                        dados.brinco()
                );

        return registrar(
                animal,
                dados.tipo(),
                dados.quantidade(),
                dados.data(),
                dados.valor()
        );
    }

    @Transactional
    public List<Alimentacao> createEmLote(
            AlimentacaoLoteRequestDTO dados,
            Usuario usuarioLogado
    ) {

        loteService.getLoteById(
                dados.loteId(),
                usuarioLogado
        );

        List<Animal> animais =
                animalService.getAnimaisAtivosByLoteId(
                        dados.loteId()
                );

        if (animais.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O lote não possui animais ativos"
            );
        }

        return animais.stream()
                .map(animal ->
                        registrar(
                                animal,
                                dados.tipo(),
                                dados.quantidade(),
                                dados.data(),
                                dados.valor()
                        )
                )
                .toList();
    }

    public List<Alimentacao> getHistorico(int brinco) {

        Animal animal =
                animalService.getAnimalByBrinco(brinco);

        return alimentacaoRepository
                .findByAnimalIdOrderByDataAscIdAsc(
                        animal.getId()
                );
    }

    private Alimentacao registrar(
            Animal animal,
            TipoAlimentacao tipo,
            double quantidade,
            LocalDate data,
            Double valor
    ) {

        animalService.validarDataRegistro(
                animal,
                data
        );

        Alimentacao alimentacao = new Alimentacao();

        alimentacao.setTipo(tipo);
        alimentacao.setQuantidade(quantidade);
        alimentacao.setValor(valor);
        alimentacao.setData(data);
        alimentacao.setAnimal(animal);

        Alimentacao alimentacaoSalva =
                alimentacaoRepository.save(
                        alimentacao
                );

        if (valor != null) {

            custoService.registrarAutomatico(
                    TipoCusto.ALIMENTACAO,
                    valor,
                    data,
                    animal
            );
        }

        return alimentacaoSalva;
    }
}