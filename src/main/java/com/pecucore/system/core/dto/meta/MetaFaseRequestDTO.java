package com.pecucore.system.core.dto.meta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Meta de desempenho de uma fase, como faixa. A fase precisa ser uma das fases da aplicação ativa,
 * e mínimo <= máximo; as duas regras são validadas no MetaFaseService.
 */
public record MetaFaseRequestDTO(
        @NotBlank(message = "A fase precisa ser informada.")
        String fase,

        @NotNull(message = "O mínimo da meta precisa ser informado.")
        Double minimo,

        Double maximo
) {
}