package com.Pecucore.system.service;

import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Custo;
import com.Pecucore.system.model.OrigemCusto;
import com.Pecucore.system.model.TipoCusto;
import com.Pecucore.system.repository.CustoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CustoAutomaticoService {

    @Autowired
    private CustoRepository custoRepository;

    @Transactional
    public Custo registrarAutomatico(
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
    public Custo atualizarAutomatico(
            Custo custo,
            double valor,
            LocalDate data) {

        custo.setValor(valor);
        custo.setData(data);

        return custoRepository.save(custo);
    }

    @Transactional
    public Custo sincronizarAutomatico(
            Custo custoAtual,
            TipoCusto tipo,
            Double valor,
            LocalDate data,
            Animal animal) {

        if (valor == null) {
            if (custoAtual != null) {
                excluirAutomatico(custoAtual);
            }

            return null;
        }

        if (custoAtual == null) {
            return registrarAutomatico(tipo, valor, data, animal);
        }

        return atualizarAutomatico(custoAtual, valor, data);
    }

    @Transactional
    public void excluirAutomatico(Custo custo) {
        custoRepository.delete(custo);
    }
}