package com.pecucore.system.core.service;

import com.pecucore.system.core.dto.medicamento.MedicamentoRequestDTO;
import com.pecucore.system.core.dto.medicamento.MedicamentoUpdateRequestDTO;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Custo;
import com.pecucore.system.core.model.Medicamento;
import com.pecucore.system.core.model.TipoCusto;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.MedicamentoRepository;
import com.pecucore.system.bovinos.sanidade.CalculadoraCarencia;
import com.pecucore.system.core.service.custo.CustoAutomaticoService;
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
    public Medicamento createMedicamento(MedicamentoRequestDTO dados, Usuario usuarioLogado) {

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
    public Medicamento updateMedicamento(Long id, MedicamentoUpdateRequestDTO dados, Usuario usuarioLogado) {

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
    public void deleteMedicamento(Long id, Usuario usuarioLogado) {
        Medicamento medicamento = getMedicamentoById(id, usuarioLogado);
        Custo custo = medicamento.getCusto();

        medicamentoRepository.delete(medicamento);

        if (custo != null) {
            custoAutomaticoService.deleteCustoAutomatico(custo);
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

        return getHistoricoByAnimalId(animal.getId());
    }

    public List<Medicamento> getHistoricoByAnimalId(Long animalId) {

        return medicamentoRepository
                .findByAnimalIdOrderByDataAscIdAsc(animalId);
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
                calculadoraCarencia.calcularDataFimCarencia(
                        data,
                        carenciaDias
                )
        );

        medicamento.setCusto(
                custoAutomaticoService.sincronizarCustoAutomatico(
                        medicamento.getCusto(),
                        TipoCusto.MEDICAMENTO,
                        valor,
                        data,
                        medicamento.getAnimal()
                )
        );
    }
}
