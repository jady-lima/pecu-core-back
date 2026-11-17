package com.pecucore.system.core.service.pesagem;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.service.animal.AnimalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pecucore.system.core.dto.pesagem.PesagemRequestDTO;
import com.pecucore.system.core.evento.PesagemRegistradaEvento;
import com.pecucore.system.core.evento.PesoAnimalAlteradoEvento;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Pesagem;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.repository.PesagemRepository;
import com.pecucore.system.core.extensao.desempenho.IndicadorDesempenho;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class PesagemService {

    @Autowired
    private PesagemRepository pesagemRepository;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private IndicadorDesempenho indicadorDesempenho;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${pesagem.intervalo-recomendado-dias}")
    private int intervaloRecomendadoDias;

    @EventListener
    @Transactional
    public void tratarPesoAnimalAlterado(PesoAnimalAlteradoEvento evento) {
        Animal animal = animalService.getAnimalAtivoById(evento.animalId());
        registrar(animal, evento.pesoKg(), evento.data());
    }

    @Transactional
    public ResultadoPesagem createPesagem(PesagemRequestDTO dados, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalAtivoByBrinco(dados.brinco(), usuarioLogado);
        return registrar(animal, dados.pesoAtual(), dados.dataPesagem());
    }

    private ResultadoPesagem registrar(Animal animal, double pesoKg, LocalDate dataPesagem) {

        animalService.validarDataRegistro(animal, dataPesagem);

        Pesagem ultimaPesagem = pesagemRepository.findTopByAnimalIdOrderByDataDescIdDesc(animal.getId()).orElse(null);

        if(ultimaPesagem != null && dataPesagem.isBefore(ultimaPesagem.getData())){
            throw new ConflitoNegocioException(
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

        Double indiceDesempenho = indicadorDesempenho.calcular(ultimaPesagem, pesoKg, dataPesagem);

        Pesagem pesagem = new Pesagem();

        pesagem.setPesoKg(pesoKg);
        pesagem.setIndiceDesempenho(indiceDesempenho);
        pesagem.setData(dataPesagem);
        pesagem.setAnimal(animal);

        pesagemRepository.save(pesagem);
        animalService.updatePesoAtual(animal, pesoKg);
        eventPublisher.publishEvent(new PesagemRegistradaEvento(animal.getId()));

        return new ResultadoPesagem(pesagem, aviso);
    }

    public List<Pesagem> getHistorico(int brinco, Usuario usuarioLogado) {

        Animal animal = animalService.getAnimalByBrinco(brinco, usuarioLogado);

        return getHistoricoByAnimalId(animal.getId());
    }

    public List<Pesagem> getHistoricoByAnimalId(Long animalId) {
        return pesagemRepository.findByAnimalIdOrderByDataAscIdAsc(animalId);
    }

    public Optional<Pesagem> getUltimaPesagem(Long animalId) {
        return pesagemRepository.findTopByAnimalIdOrderByDataDescIdDesc(animalId);
    }

    public List<Pesagem> getUltimasPesagens(Long animalId, int quantidade) {
        return pesagemRepository.findByAnimalIdOrderByDataDescIdDesc(animalId, Limit.of(quantidade));
    }

    public List<Pesagem> getPesagensAnimaisAtivosByLoteId(Long loteId) {
        return pesagemRepository.findByAnimalLoteIdAndAnimalStatusOrderByAnimalIdAscDataAscIdAsc(
                loteId,
                StatusAnimal.ATIVO
        );
    }
}
