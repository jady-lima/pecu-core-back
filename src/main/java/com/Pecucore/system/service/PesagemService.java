package com.Pecucore.system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.PesagemRequestDTO;
import com.Pecucore.system.evento.PesoAnimalAlteradoEvento;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.repository.AnimalRepository;
import com.Pecucore.system.repository.PesagemRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PesagemService {

    @Autowired
    private PesagemRepository pesagemRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private GmdService gmdService;

    @Autowired
    private AlertaService alertaService;

    @Value("${pesagem.intervalo-recomendado-dias}")
    private int intervaloRecomendadoDias;

    @EventListener
    @Transactional
    public void handlePesoAnimalAlterado(PesoAnimalAlteradoEvento evento) {
        Animal animal = animalService.getAnimalAtivoById(evento.animalId());
        registrar(animal, evento.pesoKg(), evento.data());
    }

    @Transactional
    public ResultadoPesagem create(PesagemRequestDTO dados) {
        Animal animal = animalService.getAnimalAtivoByBrinco(dados.brinco());
        return registrar(animal, dados.pesoAtual(), dados.dataPesagem());
    }

    private ResultadoPesagem registrar(Animal animal, double pesoKg, LocalDate dataPesagem) {

        animalService.validarDataRegistro(animal, dataPesagem);

        Pesagem ultimaPesagem = pesagemRepository.findTopByAnimalIdOrderByDataDescIdDesc(animal.getId()).orElse(null);

        if(ultimaPesagem != null && dataPesagem.isBefore(ultimaPesagem.getData())){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A data da pesagem não pode ser anterior à última pesagem registrada"
            );
        }

        String aviso = null;
        if(ultimaPesagem !=null) {
            long diasEntrePesagens = ChronoUnit.DAYS.between(
                    ultimaPesagem.getData(),
                    dataPesagem
            );

            if(diasEntrePesagens > intervaloRecomendadoDias){
                aviso = "O intervalo desde a última pesagem (" + diasEntrePesagens
                        + " dias) ultrapassa o recomendado (" + intervaloRecomendadoDias + " dias)";
            }
        }

        Double gmd = gmdService.calculateGmd(ultimaPesagem, pesoKg, dataPesagem);

        Pesagem pesagem = new Pesagem();

        pesagem.setPesoKg(pesoKg);
        pesagem.setGmdCalculado(gmd);
        pesagem.setData(dataPesagem);
        pesagem.setAnimal(animal);

        animal.setPesoAtual(pesoKg);

        pesagemRepository.save(pesagem);
        animalRepository.save(animal);
        alertaService.verificarGmdAbaixoMeta(animal, gmd);

        return new ResultadoPesagem(pesagem, aviso);
    }

    public List<Pesagem> getHistorico(int brinco) {

        Animal animal = animalService.getAnimalByBrinco(brinco);

        return pesagemRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());
    }
}
