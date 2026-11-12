package com.pecucore.system.core.dto.erro;

import io.swagger.v3.oas.annotations.media.Schema;

public record CampoInvalidoDTO(
        @Schema(description = "Nome do campo inválido", example = "brinco")
        String campo,

        @Schema(description = "O que está errado no campo", example = "O brinco do animal precisa ser informado!!")
        String mensagem
) {
}