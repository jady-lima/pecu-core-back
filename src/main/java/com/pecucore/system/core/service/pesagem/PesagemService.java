package com.pecucore.system.core.service.pesagem;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RegraNegocioException;
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
import com.pecucore.system.core.extensao.desempenho.EscalaCondicaoCorporal;
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
        registrar(animal, evento.pesoKg(), evento.data(), null);
    }

    @Transactional
    public ResultadoPesagem createPesagem(PesagemRequestDTO dados, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalAtivoByBrinco(dados.brinco(), usuarioLogado);
        return registrar(animal, dados.pesoAtual(), dados.dataPesagem(), dados.escoreCorporal());
    }

    public int getIntervaloRecomendadoDias(Animal animal) {
        return getFrequenciaPesagemDaPropriedade(animal).orElse(intervaloRecomendadoDias);
    }

    private void validarEscoreCorporal(Double escoreCorporal) {
        if (escoreCorporal == null) {
            return;
        }

        EscalaCondicaoCorporal escala = indicadorDesempenho.getEscalaCondicaoCorporal();

        if (!escala.aceita(escoreCorporal)) {
            throw new RegraNegocioException(
                    "O escore de condição corporal deve seguir a escala " + escala.descrever() + "."
            );
        }
    }

    private Optional<Integer> getFrequenciaPesagemDaPropriedade(Animal animal) {
        if (animal.getLote() == null || animal.getLote().getPropriedade() == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(animal.getLote().getPropriedade().getFrequenciaPesagemDias());
    }

    private ResultadoPesagem registrar(Animal animal, double pesoKg, LocalDate dataPesagem, Double escoreCorporal) {

        animalService.validarDataRegistro(animal, dataPesagem);
        validarEscoreCorporal(escoreCorporal);

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
            int intervaloRecomendado = getIntervaloRecomendadoDias(animal);

            if(diasEntrePesagens > intervaloRecomendado){
                aviso = "O intervalo desde a última pesagem (" + diasEntrePesagens
                        + " dias) ultrapassa o recomendado (" + intervaloRecomendado + " dias)";
            }
        }

        Double indiceDesempenho = indicadorDesempenho.calcular(ultimaPesagem, pesoKg, dataPesagem);

        Pesagem pesagem = new Pesagem();

        pesagem.setPesoKg(pesoKg);
        pesagem.setIndiceDesempenho(indiceDesempenho);
        pesagem.setEscoreCorporal(escoreCorporal);
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
