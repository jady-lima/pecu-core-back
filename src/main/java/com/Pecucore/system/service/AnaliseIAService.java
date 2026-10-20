package com.Pecucore.system.service;

import com.Pecucore.system.dto.analise.ContextoAnaliseAnimalDTO;
import com.Pecucore.system.dto.analise.ContextoAnaliseComparativaDTO;
import com.Pecucore.system.dto.analise.RelatorioAnaliseDTO;
import com.Pecucore.system.exception.AcessoNegadoException;
import com.Pecucore.system.exception.RecursoNaoEncontradoException;
import com.Pecucore.system.ia.AnaliseAnimalIAFacade;
import com.Pecucore.system.ia.ResultadoAnaliseIA;
import com.Pecucore.system.model.AnaliseIA;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.TipoAnalise;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.AnaliseIARepository;
import com.Pecucore.system.service.politica.PoliticaVisibilidadeFinanceira;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnaliseIAService {

    @Autowired
    private AnaliseIARepository analiseIARepository;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ContextoAnaliseService contextoAnaliseService;

    @Autowired
    private AnaliseAnimalIAFacade analiseAnimalIAFacade;

    @Autowired
    private PoliticaVisibilidadeFinanceira politicaVisibilidadeFinanceira;

    @Autowired
    private ContextoComparativoService contextoComparativoService;

    public AnaliseIA gerarIndividual(Long animalId, Usuario usuarioLogado) {
        ContextoAnaliseAnimalDTO contexto = contextoAnaliseService.montarIndividual(animalId, usuarioLogado);
        ResultadoAnaliseIA resultado = analiseAnimalIAFacade.analisarIndividual(contexto);

        return salvar(TipoAnalise.INDIVIDUAL, animalId, resultado, contexto.custos() != null, usuarioLogado);
    }

    public AnaliseIA gerarComparativa(Long animalId, Usuario usuarioLogado) {
        ContextoAnaliseComparativaDTO contexto = contextoComparativoService.montarComparativo(animalId, usuarioLogado);
        ResultadoAnaliseIA resultado = analiseAnimalIAFacade.analisarComparativo(contexto);

        boolean incluiDadosFinanceiros = contexto.animalAnalisado().custos() != null || contexto.estatisticasLote().custoMedioPorAnimal() != null;

        return salvar(TipoAnalise.COMPARATIVA, animalId, resultado, incluiDadosFinanceiros, usuarioLogado);
    }

    public List<AnaliseIA> getHistoricoByAnimal(Long animalId, Usuario usuarioLogado) {
        animalService.getAnimalById(animalId, usuarioLogado);

        return analiseIARepository.findByAnimalIdOrderByDataGeracaoDesc(animalId).stream()
                                  .filter(analise -> podeVisualizar(analise, usuarioLogado))
                                  .toList();
    }

    public AnaliseIA getAnaliseById(Long id, Usuario usuarioLogado) {
        AnaliseIA analise = analiseIARepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException( "Análise não encontrada"));

        animalService.getAnimalById(analise.getAnimal().getId(), usuarioLogado);

        if (!podeVisualizar(analise, usuarioLogado)) {
            throw new AcessoNegadoException( "Você não tem acesso a esta análise");
        }

        return analise;
    }

    public RelatorioAnaliseDTO getRelatorio(AnaliseIA analise) {
        return analiseAnimalIAFacade.lerRelatorio(analise.getConteudo());
    }

    private AnaliseIA salvar(TipoAnalise tipo, Long animalId, ResultadoAnaliseIA resultado, boolean incluiDadosFinanceiros, Usuario usuarioLogado) {
        Animal animal = animalService.getAnimalById(animalId, usuarioLogado);
        Usuario solicitante = usuarioService.getUsuarioById(usuarioLogado.getId());

        AnaliseIA analise = new AnaliseIA();
        analise.setTipo(tipo);
        analise.setAnimal(animal);
        analise.setSolicitante(solicitante);
        analise.setConteudo(resultado.conteudoBruto());
        analise.setModeloUtilizado(resultado.modelo());
        analise.setIncluiDadosFinanceiros(incluiDadosFinanceiros);

        return analiseIARepository.save(analise);
    }

    private boolean podeVisualizar(AnaliseIA analise, Usuario usuarioLogado) {
        return !analise.isIncluiDadosFinanceiros() || politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuarioLogado);
    }
}
