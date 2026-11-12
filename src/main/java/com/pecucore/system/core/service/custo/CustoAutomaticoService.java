package com.pecucore.system.core.service.custo;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Custo;
import com.pecucore.system.core.model.OrigemCusto;
import com.pecucore.system.core.model.TipoCusto;
import com.pecucore.system.core.repository.CustoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CustoAutomaticoService {

    @Autowired
    private CustoRepository custoRepository;

    @Transactional
    public Custo createCustoAutomatico(
            TipoCusto tipo,
            double valor,
            LocalDate data,
            Animal animal) {

        Custo custo = new Custo();

        custo.setTipo(tipo);
        custo.setOrigem(OrigemCusto.AUTOMATICO);
        custo.setValor(valor);
        custo.setData(data);
        custo.setAnimal(animal);

        return custoRepository.save(custo);
    }

    @Transactional
    public Custo updateCustoAutomatico(
            Custo custo,
            double valor,
            LocalDate data) {

        custo.setValor(valor);
        custo.setData(data);

        return custoRepository.save(custo);
    }

    @Transactional
    public Custo sincronizarCustoAutomatico(
            Custo custoAtual,
            TipoCusto tipo,
            Double valor,
            LocalDate data,
            Animal animal) {

        if (valor == null) {
            if (custoAtual != null) {
                deleteCustoAutomatico(custoAtual);
            }

            return null;
        }

        if (custoAtual == null) {
            return createCustoAutomatico(tipo, valor, data, animal);
        }

        return updateCustoAutomatico(custoAtual, valor, data);
    }

    @Transactional
    public void deleteCustoAutomatico(Custo custo) {
        custoRepository.delete(custo);
    }
}