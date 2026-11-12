package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.alerta.AlertaResponseDTO;
import com.pecucore.system.core.model.StatusAlerta;
import com.pecucore.system.core.service.alerta.AlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alertas")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @GetMapping
    public ResponseEntity<List<AlertaResponseDTO>> listarAlertas() {
        List<AlertaResponseDTO> alertas = alertaService.listarAlertas().stream()
                .map(AlertaResponseDTO::new)
                .toList();

        return ResponseEntity.ok(alertas);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AlertaResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusAlerta status
    ) {
        return ResponseEntity.ok(
                new AlertaResponseDTO(alertaService.atualizarStatus(id, status))
        );
    }
}