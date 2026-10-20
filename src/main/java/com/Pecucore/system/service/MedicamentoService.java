package com.Pecucore.system.service;

import com.Pecucore.system.dto.MedicamentoRequestDTO;
import com.Pecucore.system.dto.MedicamentoUpdateRequestDTO;
import com.Pecucore.system.exception.RecursoNaoEncontradoException;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Custo;
import com.Pecucore.system.model.Medicamento;
import com.Pecucore.system.model.TipoCusto;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.MedicamentoRepository;
import com.Pecucore.system.service.calculo.CalculadoraCarencia;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private CustoAutomaticoService custoAutomaticoService;

    @Transactional
    public Medicamento create(MedicamentoRequestDTO dados, Usuario usuarioLogado) {

        Animal animal = animalService.getAnimalAtivoByBrinco(dados.brinco(), usuarioLogado);

        animalService.validarDataRegistro(animal, dados.data());

        Medicamento medicamento = new Medicamento();

        medicamento.setAnimal(animal);

        preencherDados(
                medicamento,
                dados.nome(),
                dados.motivo(),
                dados.dosagem(),
                dados.carenciaDias(),
                dados.data(),
                dados.valor()
        );

        return medicamentoRepository.save(medicamento);
    }

    @Transactional
    public Medicamento update(Long id, MedicamentoUpdateRequestDTO dados, Usuario usuarioLogado) {

        Medicamento medicamento = getMedicamentoById(id, usuarioLogado);

        animalService.validarDataRegistro(medicamento.getAnimal(), dados.data());

        preencherDados(
                medicamento,
                dados.nome(),
                dados.motivo(),
                dados.dosagem(),
                dados.carenciaDias(),
                dados.data(),
                dados.valor()
        );

        return medicamentoRepository.save(medicamento);
    }

    @Transactional
    public void delete(Long id, Usuario usuarioLogado) {
        Medicamento medicamento = getMedicamentoById(id, usuarioLogado);
        Custo custo = medicamento.getCusto();

        medicamentoRepository.delete(medicamento);

        if (custo != null) {
            custoAutomaticoService.excluirAutomatico(custo);
        }
    }

    public Medicamento getMedicamentoById(Long id, Usuario usuarioLogado) {

        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Medicamento não encontrado"
                ));

        animalService.getAnimalById(medicamento.getAnimal().getId(), usuarioLogado);

        return medicamento;
    }

    public List<Medicamento> getHistorico(int brinco, Usuario usuarioLogado) {

        Animal animal = animalService.getAnimalByBrinco(brinco, usuarioLogado);

        return medicamentoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animal.getId());
    }

    private void preencherDados(
            Medicamento medicamento,
            String nome,
            String motivo,
            Double dosagem,
            Integer carenciaDias,
            LocalDate data,
            Double valor
    ) {

        medicamento.setData(data);
        medicamento.setNome(nome.trim());
        medicamento.setMotivo(motivo.trim());
        medicamento.setDosagem(dosagem);
        medicamento.setCarenciaDias(carenciaDias);
        medicamento.setValor(valor);

        medicamento.setDataFimCarencia(
                calculadoraCarencia.calculateDataFimCarencia(
                        data,
                        carenciaDias
                )
        );

        medicamento.setCusto(
                custoAutomaticoService.sincronizarAutomatico(
                        medicamento.getCusto(),
                        TipoCusto.MEDICAMENTO,
                        valor,
                        data,
                        medicamento.getAnimal()
                )
        );
    }
}
