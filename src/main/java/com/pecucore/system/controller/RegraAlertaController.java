package com.pecucore.system.controller;

import com.pecucore.system.model.RegraAlerta;
import com.pecucore.system.model.TipoAlerta;
import com.pecucore.system.service.alerta.RegraAlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/regras-alerta")
public class RegraAlertaController {

    @Autowired
    private RegraAlertaService regraAlertaService;

    @PostMapping
    public RegraAlerta criarRegra(
            @RequestParam Long propriedadeId,
            @RequestParam TipoAlerta tipo,
            @RequestParam double parametro
    ) {
        return regraAlertaService.criarRegra(
                propriedadeId, tipo, parametro
        );
    }

    @PutMapping("/{id}")
    public RegraAlerta atualizarRegra(
            @PathVariable Long id,
            @RequestParam double parametro,
            @RequestParam boolean ativa
    ) {
        return regraAlertaService.atualizarRegra(id, parametro, ativa);
    }
}