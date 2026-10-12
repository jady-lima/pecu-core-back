package com.Pecucore.system.controller;

import com.Pecucore.system.model.RegraAlerta;
import com.Pecucore.system.service.RegraAlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/regras-alerta")
public class RegraAlertaController {

    @Autowired
    private RegraAlertaService regraAlertaService;

    @PutMapping("/{id}")
    public RegraAlerta atualizarRegra(
            @PathVariable Long id,
            @RequestParam double parametro,
            @RequestParam boolean ativa
    ) {
        return regraAlertaService.atualizarRegra(id, parametro, ativa);
    }
}