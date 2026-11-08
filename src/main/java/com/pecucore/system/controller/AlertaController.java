package com.pecucore.system.controller;

import com.pecucore.system.model.Alerta;
import com.pecucore.system.model.StatusAlerta;
import com.pecucore.system.service.alerta.AlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alertas")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @GetMapping
    public List<Alerta> listarAlertas() {
        return alertaService.listarAlertas();
    }

    @PutMapping("/{id}/status")
    public Alerta atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusAlerta status
    ) {
        return alertaService.atualizarStatus(id, status);
    }
}