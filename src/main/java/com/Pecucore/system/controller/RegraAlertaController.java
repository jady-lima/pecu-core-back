package com.Pecucore.system.controller;

import com.Pecucore.system.model.RegraAlerta;
import com.Pecucore.system.model.TipoAlerta;
import com.Pecucore.system.service.RegraAlertaService;
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