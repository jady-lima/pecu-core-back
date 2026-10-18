package com.Pecucore.system.service;

import com.Pecucore.system.model.TipoCusto;
import com.Pecucore.system.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.Pecucore.system.dto.AlimentacaoLoteRequestDTO;
import com.Pecucore.system.dto.AlimentacaoRequestDTO;
import com.Pecucore.system.dto.AlimentacaoUpdateRequestDTO;
import com.Pecucore.system.model.Alimentacao;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.Custo;
import com.Pecucore.system.model.TipoAlimentacao;
import com.Pecucore.system.repository.AlimentacaoRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class AlimentacaoService {

    @Autowired
    private AlimentacaoRepository alimentacaoRepository;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private LoteService loteService;

    @Autowired
    private CustoService custoService;

    @Transactional
    public Alimentacao create(AlimentacaoRequestDTO dados, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalAtivoByBrinco(dados.brinco(), usuarioLogado);

        return registrar(
                animal,
                dados.tipo(),
                dados.quantidade(),
                dados.data(),
                dados.valor()
        );
    }

    @Transactional
    public List<Alimentacao> createEmLote(AlimentacaoLoteRequestDTO dados, Usuario usuarioLogado) {
        loteService.getLoteById(dados.loteId(), usuarioLogado);

        List<Animal> animais = animalService.getAnimaisAtivosByLoteId(dados.loteId());

        if (animais.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "O lote não possui animais ativos"
            );
        }

        return animais.stream()
                .map(animal ->
                        registrar(
                                animal,
                                dados.tipo(),
                                dados.quantidade(),
                                dados.data(),
                                dados.valor()
                        )
                )
                .toList();
    }

    @Transactional
    public Alimentacao update(Long id, AlimentacaoUpdateRequestDTO dados, Usuario usuarioLogado) {
        Alimentacao alimentacao = getAlimentacaoById(id, usuarioLogado);

        animalService.validarDataRegistro(alimentacao.getAnimal(), dados.data());

        alimentacao.setTipo(dados.tipo());
        alimentacao.setQuantidade(dados.quantidade());
        alimentacao.setValor(dados.valor());
        alimentacao.setData(dados.data());

        sincronizarCusto(alimentacao);

        return alimentacaoRepository.save(alimentacao);
    }

    @Transactional
    public void delete(Long id, Usuario usuarioLogado) {
        Alimentacao alimentacao = getAlimentacaoById(id, usuarioLogado);
        Custo custo = alimentacao.getCusto();

        alimentacaoRepository.delete(alimentacao);

        if (custo != null) {
            custoService.excluirAutomatico(custo);
        }
    }

    public Alimentacao getAlimentacaoById(Long id, Usuario usuarioLogado) {
        Alimentacao alimentacao = alimentacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alimentação não encontrada"
                ));

        animalService.getAnimalById(alimentacao.getAnimal().getId(), usuarioLogado);

        return alimentacao;
    }

    public List<Alimentacao> getHistorico(int brinco, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalByBrinco(brinco, usuarioLogado);

        return alimentacaoRepository.findByAnimalIdOrderByDataAscIdAsc(animal.getId());
    }

    private Alimentacao registrar(
            Animal animal,
            TipoAlimentacao tipo,
            double quantidade,
            LocalDate data,
            Double valor
    ) {

        animalService.validarDataRegistro(animal, data);

        Alimentacao alimentacao = new Alimentacao();
        alimentacao.setTipo(tipo);
        alimentacao.setQuantidade(quantidade);
        alimentacao.setValor(valor);
        alimentacao.setData(data);
        alimentacao.setAnimal(animal);

        sincronizarCusto(alimentacao);

        return alimentacaoRepository.save(alimentacao);
    }

    private void sincronizarCusto(Alimentacao alimentacao) {
        alimentacao.setCusto(custoService.sincronizarAutomatico(
                alimentacao.getCusto(),
                TipoCusto.ALIMENTACAO,
                alimentacao.getValor(),
                alimentacao.getData(),
                alimentacao.getAnimal()
        ));
    }
}