package com.pecucore.system.core.controller;

import com.pecucore.system.core.dto.alerta.RegraAlertaResponseDTO;
import com.pecucore.system.core.model.RegraAlerta;
import com.pecucore.system.core.model.TipoAlerta;
import com.pecucore.system.core.service.alerta.RegraAlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/regras-alerta")
public class RegraAlertaController {

    @Autowired
    private RegraAlertaService regraAlertaService;

    @PostMapping
    public ResponseEntity<RegraAlertaResponseDTO> criarRegra(
            @RequestParam Long propriedadeId,
            @RequestParam TipoAlerta tipo,
            @RequestParam double parametro
    ) {
        RegraAlerta regra = regraAlertaService.criarRegra(propriedadeId, tipo, parametro);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegraAlertaResponseDTO(regra));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegraAlertaResponseDTO> atualizarRegra(
            @PathVariable Long id,
            @RequestParam double parametro,
            @RequestParam boolean ativa
    ) {
        RegraAlerta regra = regraAlertaService.atualizarRegra(id, parametro, ativa);

        return ResponseEntity.ok(new RegraAlertaResponseDTO(regra));
    }
}