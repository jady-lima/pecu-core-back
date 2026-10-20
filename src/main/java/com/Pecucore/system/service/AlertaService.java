package com.Pecucore.system.service;

import com.Pecucore.system.exception.RecursoNaoEncontradoException;
import com.Pecucore.system.model.*;
import com.Pecucore.system.repository.AlertaRepository;
import com.Pecucore.system.repository.PesagemRepository;
import com.Pecucore.system.repository.RegraAlertaRepository;
import com.Pecucore.system.repository.VacinacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private RegraAlertaRepository regraAlertaRepository;

    @Autowired
    private PesagemRepository pesagemRepository;

    @Autowired
    private VacinacaoRepository vacinacaoRepository;


    public List<Alerta> listarAlertas() {
        return alertaRepository.findAll();
    }

    public Alerta atualizarStatus(Long id, StatusAlerta novoStatus) {

        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Alerta não encontrado"
                ));
        alerta.setStatus(novoStatus);
        return alertaRepository.save(alerta);
    }




    public void criarAlerta(
            Animal animal,
            TipoAlerta tipo,
            String mensagem
    ) {
        Alerta alerta = new Alerta();

        alerta.setAnimal(animal);
        alerta.setTipo(tipo);
        alerta.setSeveridade(SeveridadeAlerta.MEDIA);
        alerta.setMensagem(mensagem);
        alerta.setStatus(StatusAlerta.ABERTO);
        alerta.setDataCriacao(java.time.LocalDateTime.now());

        alertaRepository.save(alerta);

    }
    }



