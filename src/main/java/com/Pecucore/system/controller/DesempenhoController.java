package com.Pecucore.system.controller;

import com.Pecucore.system.dto.ComparacaoDesempenhoResponseDTO;
import com.Pecucore.system.dto.CurvaCrescimentoResponseDTO;
import com.Pecucore.system.service.DesempenhoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/desempenho")
public class DesempenhoController {

    @Autowired
    private DesempenhoService desempenhoService;

    @GetMapping("/animal/{brinco}/curva")
    public ResponseEntity<List<CurvaCrescimentoResponseDTO>> getCurvaCrescimento(
            @PathVariable int brinco
    ) {

        List<CurvaCrescimentoResponseDTO> curva =
                desempenhoService.getCurvaCrescimento(brinco);

        return ResponseEntity.ok(curva);
    }

    @GetMapping("/animal/{brinco}/comparacao")
    public ResponseEntity<ComparacaoDesempenhoResponseDTO> comparar(
            @PathVariable int brinco
    ) {

        ComparacaoDesempenhoResponseDTO resultado =
                desempenhoService.comparar(brinco);

        return ResponseEntity.ok(resultado);
    }
}