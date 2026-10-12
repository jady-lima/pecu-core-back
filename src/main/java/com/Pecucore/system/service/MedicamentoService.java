package com.Pecucore.system.service;

import com.Pecucore.system.dto.MedicamentoRequestDTO;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Medicamento;
import com.Pecucore.system.model.TipoCusto;
import com.Pecucore.system.repository.MedicamentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private CalculadoraCarencia calculadoraCarencia;

    @Autowired
    private CustoService custoService;

    @Transactional
    public Medicamento create(MedicamentoRequestDTO dados) {

        Animal animal = animalService.getAnimalAtivoByBrinco(dados.brinco());

        animalService.validarDataRegistro(animal, dados.data());

        Medicamento medicamento = new Medicamento();

        medicamento.setAnimal(animal);
        medicamento.setData(dados.data());
        medicamento.setNome(dados.nome().trim());
        medicamento.setMotivo(dados.motivo().trim());
        medicamento.setDosagem(dados.dosagem());
        medicamento.setCarenciaDias(dados.carenciaDias());
        medicamento.setValor(dados.valor());

        medicamento.setDataFimCarencia(
                calculadoraCarencia.calculateDataFimCarencia(
                        dados.data(),
                        dados.carenciaDias()
                )
        );

        Medicamento medicamentoSalvo = medicamentoRepository.save(medicamento);

        if (dados.valor() != null) {
            custoService.registrarAutomatico(
                    TipoCusto.MEDICAMENTO,
                    dados.valor(),
                    dados.data(),
                    animal
            );
        }

        return medicamentoSalvo;
    }

    public List<Medicamento> getHistorico(int brinco) {

        Animal animal = animalService.getAnimalByBrinco(brinco);

        return medicamentoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());
    }
}
