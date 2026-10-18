package com.Pecucore.system.service;

import com.Pecucore.system.dto.CustoRequestDTO;
import com.Pecucore.system.model.*;
import com.Pecucore.system.repository.CustoRepository;
import com.Pecucore.system.repository.LoteRepository;
import com.Pecucore.system.repository.PropriedadeRepository;
import com.Pecucore.system.repository.AnimalRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import java.util.List;

@Service
public class CustoService {

    @Autowired
    private CustoRepository custoRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private AcessoPropriedadeService acessoPropriedadeService;

    @Autowired
    private AlertaService alertaService;

    @Transactional
    public Custo create(CustoRequestDTO dados, Usuario usuarioLogado) {

        validarDestino(dados);

        Custo custo = new Custo();

        custo.setTipo(dados.tipo());
        custo.setOrigem(OrigemCusto.MANUAL);
        custo.setValor(dados.valor());
        custo.setData(dados.data());

        if (dados.animalId() != null) {
            Animal animal = animalRepository.findById(dados.animalId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Animal não encontrado"
                    ));

            validarAcessoPropriedade(animal.getLote() != null ? animal.getLote().getPropriedade() : null, usuarioLogado);

            custo.setAnimal(animal);
        }

        if (dados.loteId() != null) {
            Lote lote = loteRepository.findById(dados.loteId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Lote não encontrado"
                    ));

            validarAcessoPropriedade(lote.getPropriedade(), usuarioLogado);

            custo.setLote(lote);
        }

        if (dados.propriedadeId() != null) {
            Propriedade propriedade = propriedadeRepository.findById(dados.propriedadeId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Propriedade não encontrada"
                    ));

            validarAcessoPropriedade(propriedade, usuarioLogado);

            custo.setPropriedade(propriedade);
        }

        Custo custoSalvo = custoRepository.save(custo);
        if(custoSalvo.getAnimal() !=null){
            alertaService.verificarCustoAcima(custoSalvo.getAnimal());
        }
        return custoSalvo;
    }

    private void validarAcessoPropriedade(Propriedade propriedade, Usuario usuarioLogado) {

        if (!acessoPropriedadeService.possuiAcesso(propriedade, usuarioLogado)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem acesso a esta propriedade"
            );
        }
    }

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

        Custo custoSalvo = custoRepository.save(custo);
        if(custoSalvo.getAnimal() !=null){
            alertaService.verificarCustoAcima(custoSalvo.getAnimal());
        }
        return custoSalvo;

    }

    private void validarDestino(CustoRequestDTO dados) {

        int destinos = 0;

        if (dados.animalId() != null) {
            destinos++;
        }

        if (dados.loteId() != null) {
            destinos++;
        }

        if (dados.propriedadeId() != null) {
            destinos++;
        }

        if (destinos != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O custo deve estar associado a um animal, lote ou propriedade."
            );
        }
    }

    public List<Custo> getAll(Usuario usuarioLogado) {
        return custoRepository.findByProdutor(usuarioLogado.getId());
    }
}