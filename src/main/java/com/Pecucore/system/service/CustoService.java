package com.Pecucore.system.service;

import com.Pecucore.system.dto.CustoRequestDTO;
import com.Pecucore.system.exception.ConflitoNegocioException;
import com.Pecucore.system.exception.RecursoNaoEncontradoException;
import com.Pecucore.system.exception.RegraNegocioException;
import com.Pecucore.system.model.*;
import com.Pecucore.system.repository.CustoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class CustoService {

    @Autowired
    private CustoRepository custoRepository;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private LoteService loteService;

    @Autowired
    private PropriedadeService propriedadeService;

    @Autowired
    private AcessoPropriedadeService acessoPropriedadeService;

    @Autowired
    private AlertaService alertaService;

    @Transactional
    public Custo create(CustoRequestDTO dados, Usuario usuarioLogado) {
        Custo custo = new Custo();
        custo.setOrigem(OrigemCusto.MANUAL);

        preencherDados(custo, dados, usuarioLogado);

        Custo custoSalvo = custoRepository.save(custo);
        if(custoSalvo.getAnimal() !=null){
            alertaService.verificarCustoAcima(custoSalvo.getAnimal());
        }
        return custoSalvo;
    }

    @Transactional
    public Custo update(Long id, CustoRequestDTO dados, Usuario usuarioLogado) {
        Custo custo = getCustoManualById(id, usuarioLogado);

        preencherDados(custo, dados, usuarioLogado);

        Custo custoSalvo = custoRepository.save(custo);
        if(custoSalvo.getAnimal() !=null){
            alertaService.verificarCustoAcima(custoSalvo.getAnimal());
        }
        return custoSalvo;

    }

    @Transactional
    public void delete(Long id, Usuario usuarioLogado) {
        Custo custo = getCustoManualById(id, usuarioLogado);

        custoRepository.delete(custo);
    }

    public Custo getCustoById(Long id, Usuario usuarioLogado) {
        Custo custo = custoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                    "Custo não encontrado"
                ));

        acessoPropriedadeService.validarAcesso(
                getPropriedade(custo),
                usuarioLogado,
                "Você não tem acesso a esta propriedade"
        );

        return custo;
    }

    private Custo getCustoManualById(Long id, Usuario usuarioLogado) {
        Custo custo = getCustoById(id, usuarioLogado);

        if (custo.getOrigem() == OrigemCusto.AUTOMATICO) {
            throw new ConflitoNegocioException(
                "Custos automáticos só podem ser alterados pelo registro que os gerou"
            );
        }

        return custo;
    }

    private Propriedade getPropriedade(Custo custo) {
        if (custo.getAnimal() != null) {
            return custo.getAnimal().getLote() != null ? custo.getAnimal().getLote().getPropriedade() : null;
        }

        if (custo.getLote() != null) {
            return custo.getLote().getPropriedade();
        }

        return custo.getPropriedade();
    }

    private void preencherDados(Custo custo, CustoRequestDTO dados, Usuario usuarioLogado) {

        validarDestino(dados);

        custo.setTipo(dados.tipo());
        custo.setValor(dados.valor());
        custo.setData(dados.data());
        custo.setAnimal(null);
        custo.setLote(null);
        custo.setPropriedade(null);

        if (dados.animalId() != null) {
            custo.setAnimal(animalService.getAnimalById(dados.animalId(), usuarioLogado));
        }

        if (dados.loteId() != null) {
            custo.setLote(loteService.getLoteById(dados.loteId(), usuarioLogado));
        }

        if (dados.propriedadeId() != null) {
            custo.setPropriedade(propriedadeService.getPropriedadeById(dados.propriedadeId(), usuarioLogado));
        }
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
            throw new RegraNegocioException(
                    "O custo deve estar associado a um animal, lote ou propriedade."
            );
        }
    }

    public List<Custo> getAll(Usuario usuarioLogado) {
        return custoRepository.findByProdutor(usuarioLogado.getId());
    }

    public List<Custo> getByAnimalId(Long animalId) {
        return custoRepository.findByAnimalIdOrderByDataAscIdAsc(animalId);
    }

    public List<Custo> getByAnimalIds(Collection<Long> animalIds) {
        return custoRepository.findByAnimalIdIn(animalIds);
    }
}