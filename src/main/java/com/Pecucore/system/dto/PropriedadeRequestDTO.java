package com.Pecucore.system.dto;


import jakarta.validation.constraints.NotBlank;

public record PropriedadeRequestDTO(
        @NotBlank(message = "O campo nome não pode ser vazio")
        String nome,

        @NotBlank(message ="A Localização deve ser informada")
        String localizacao
) {
}
