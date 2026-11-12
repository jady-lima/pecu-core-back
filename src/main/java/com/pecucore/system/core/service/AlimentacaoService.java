package com.pecucore.system.core.service;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.model.TipoCusto;
import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.custo.CustoAutomaticoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pecucore.system.core.dto.alimentacao.AlimentacaoLoteRequestDTO;
import com.pecucore.system.core.dto.alimentacao.AlimentacaoRequestDTO;
import com.pecucore.system.core.dto.alimentacao.AlimentacaoUpdateRequestDTO;
import com.pecucore.system.core.model.Alimentacao;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.Custo;
import com.pecucore.system.core.model.TipoAlimentacao;
import com.pecucore.system.core.repository.AlimentacaoRepository;

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
    private CustoAutomaticoService custoAutomaticoService;

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
            throw new ConflitoNegocioException(
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
            custoAutomaticoService.excluirAutomatico(custo);
        }
    }

    public Alimentacao getAlimentacaoById(Long id, Usuario usuarioLogado) {
        Alimentacao alimentacao = alimentacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Alimentação não encontrada"
                ));

        animalService.getAnimalById(alimentacao.getAnimal().getId(), usuarioLogado);

        return alimentacao;
    }

    public List<Alimentacao> getHistorico(int brinco, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalByBrinco(brinco, usuarioLogado);

        return getHistoricoByAnimalId(animal.getId());
    }

    public List<Alimentacao> getHistoricoByAnimalId(Long animalId) {
        return alimentacaoRepository.findByAnimalIdOrderByDataAscIdAsc(animalId);
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
        alimentacao.setCusto(custoAutomaticoService.sincronizarAutomatico(
                alimentacao.getCusto(),
                TipoCusto.ALIMENTACAO,
                alimentacao.getValor(),
                alimentacao.getData(),
                alimentacao.getAnimal()
        ));
    }
}