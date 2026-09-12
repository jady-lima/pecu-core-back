package com.Pecucore.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErroResponseDTO(@Schema(description = "Mensagem que descreve o erro ocorrido", example = "Usuário ou senha inválidos") String mensagem) {
}
