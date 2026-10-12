package com.Pecucore.system.controller;

import com.Pecucore.system.dto.MetaDesempenhoRequestDTO;
import com.Pecucore.system.dto.MetaDesempenhoResponseDTO;
import com.Pecucore.system.model.MetaDesempenho;
import com.Pecucore.system.service.MetaDesempenhoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/metas-desempenho")
public class MetaDesempenhoController {

    @Autowired
    private MetaDesempenhoService metaDesempenhoService;

    @PostMapping
    public ResponseEntity<MetaDesempenhoResponseDTO> create(
            @RequestBody @Valid MetaDesempenhoRequestDTO dados
    ) {

        MetaDesempenho meta = metaDesempenhoService.create(dados);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MetaDesempenhoResponseDTO(meta));
    }

    @GetMapping
    public ResponseEntity<List<MetaDesempenhoResponseDTO>> getAll() {

        List<MetaDesempenhoResponseDTO> metas =
                metaDesempenhoService.getAll()
                        .stream()
                        .map(MetaDesempenhoResponseDTO::new)
                        .toList();

        return ResponseEntity.ok(metas);
    }
}