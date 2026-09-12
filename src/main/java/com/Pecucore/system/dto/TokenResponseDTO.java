package com.Pecucore.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponseDTO(@Schema(description = "Token JWT utilizado para autenticar as requisições à API", example = "eyJhbGciOiJIUzI1NiJ9...") String token) {
}
