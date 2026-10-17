package com.Pecucore.system.service;

import com.Pecucore.system.dto.analise.ContextoAnaliseAnimalDTO;
import com.Pecucore.system.dto.analise.RelatorioAnaliseDTO;
import com.Pecucore.system.ia.AnaliseAnimalIAFacade;
import com.Pecucore.system.ia.ResultadoAnaliseIA;
import com.Pecucore.system.model.AnaliseIA;
import com.Pecucore.system.model.Animal;
import com.Pecucore.system.model.TipoAnalise;
import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.AnaliseIARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public AnaliseIA gerarIndividual(Long animalId, Usuario usuarioLogado) {
        
        ContextoAnaliseAnimalDTO contexto = contextoAnaliseService.montar(animalId, usuarioLogado);
        ResultadoAnaliseIA resultado = analiseAnimalIAFacade.analisar(contexto);

        Animal animal = animalService.getAnimalById(animalId, usuarioLogado);
        Usuario solicitante = usuarioService.getUsuarioById(usuarioLogado.getId());

        AnaliseIA analise = new AnaliseIA();
        analise.setTipo(TipoAnalise.INDIVIDUAL);
        analise.setAnimal(animal);
        analise.setSolicitante(solicitante);
        analise.setConteudo(resultado.conteudoBruto());
        analise.setModeloUtilizado(resultado.modelo());
        analise.setIncluiDadosFinanceiros(contexto.custos() != null);

        return analiseIARepository.save(analise);
    }

    public List<AnaliseIA> getHistoricoByAnimal(Long animalId, Usuario usuarioLogado) {
        animalService.getAnimalById(animalId, usuarioLogado);

        return analiseIARepository.findByAnimalIdOrderByDataGeracaoDesc(animalId).stream()
                                  .filter(analise -> podeVisualizar(analise, usuarioLogado))
                                  .toList();
    }

    public AnaliseIA getAnaliseById(Long id, Usuario usuarioLogado) {
        AnaliseIA analise = analiseIARepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Análise não encontrada"));

        animalService.getAnimalById(analise.getAnimal().getId(), usuarioLogado);

        if (!podeVisualizar(analise, usuarioLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a esta análise");
        }

        return analise;
    }

    public RelatorioAnaliseDTO getRelatorio(AnaliseIA analise) {
        return analiseAnimalIAFacade.lerRelatorio(analise.getConteudo());
    }

    private boolean podeVisualizar(AnaliseIA analise, Usuario usuarioLogado) {
        return !analise.isIncluiDadosFinanceiros() || politicaVisibilidadeFinanceira.podeVerDadosFinanceiros(usuarioLogado);
    }
}
