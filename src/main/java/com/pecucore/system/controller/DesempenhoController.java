package com.pecucore.system.controller;

import com.pecucore.system.dto.desempenho.ComparacaoDesempenhoResponseDTO;
import com.pecucore.system.dto.desempenho.CurvaCrescimentoResponseDTO;
import com.pecucore.system.model.Usuario;
import com.pecucore.system.service.DesempenhoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @PathVariable int brinco,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        List<CurvaCrescimentoResponseDTO> curva =
                desempenhoService.getCurvaCrescimento(brinco, usuarioLogado);

        return ResponseEntity.ok(curva);
    }

    @GetMapping("/animal/{brinco}/comparacao")
    public ResponseEntity<ComparacaoDesempenhoResponseDTO> comparar(
            @PathVariable int brinco,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {

        ComparacaoDesempenhoResponseDTO resultado =
                desempenhoService.comparar(brinco, usuarioLogado);

        return ResponseEntity.ok(resultado);
    }
}