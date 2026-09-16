package com.Pecucore.system.service;

import com.Pecucore.system.model.StatusAnimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.PesagemRequestDTO;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.repository.AnimalRepository;
import com.Pecucore.system.repository.PesagemRepository;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PesagemService {

    @Autowired
    private PesagemRepository pesagemRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Value("${pesagem.intervalo-recomendado-dias}")
    private int intervaloRecomendadoDias;

    public Pesagem create(PesagemRequestDTO dados) {

        Animal animal = animalRepository.findById(dados.animalId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Animal não encontrado"
                ));
        if (animal.getStatus() != StatusAnimal.ATIVO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possivel registrar pesagem para um animal que não está ativo"
            );
        }
        Pesagem ultimaPesagem = pesagemRepository.findTopByAnimalIdOrderByDataPesagemDesc(dados.animalId()).orElse(null);

        if(ultimaPesagem != null && dados.dataPesagem().isBefore(ultimaPesagem.getDataPesagem())){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A data da pesagem não pode ser anterior á ultima pesagem registrada"
            );
        }

        double variacaoPeso = 0;
        if(ultimaPesagem !=null){
            variacaoPeso = dados.pesoAtual() - ultimaPesagem.getPeso();
        }

        long diasEntrePesagens = 0;
        double gmd = 0;
        if(ultimaPesagem !=null) {
            diasEntrePesagens = ChronoUnit.DAYS.between(
                    ultimaPesagem.getDataPesagem(),
                    dados.dataPesagem()

            );

            if(diasEntrePesagens > intervaloRecomendadoDias){
                System.out.println("Aviso: O intervalo recomendado entre as pesagens foi ultrapassado");
            }

            if (diasEntrePesagens > 0) {

                gmd = variacaoPeso / diasEntrePesagens;
            }
        }

        Pesagem pesagem = new Pesagem();

        pesagem.setPeso(dados.pesoAtual());
        pesagem.setDataPesagem(dados.dataPesagem());
        pesagem.setAnimal(animal);

        animal.setPesoAtual(dados.pesoAtual());

        pesagemRepository.save(pesagem);
        animalRepository.save(animal);

        return pesagem;
    }
    public List<Pesagem> getHistorico(Long animalId) {

        if (!animalRepository.existsById(animalId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Animal não encontrado"
            );
        }

        return pesagemRepository.findByAnimalIdOrderByDataPesagemAsc(animalId);
    }
}