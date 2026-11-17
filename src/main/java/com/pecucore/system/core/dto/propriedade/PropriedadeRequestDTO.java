package com.pecucore.system.core.dto.propriedade;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PropriedadeRequestDTO(
        @NotBlank(message = "O campo nome não pode ser vazio")
        String nome,

        @NotBlank(message ="A Localização deve ser informada")
        String localizacao,

        @Positive(message = "A frequência de pesagem deve ser maior que zero")
        Integer frequenciaPesagemDias,

        Long usuarioId
) {
}
