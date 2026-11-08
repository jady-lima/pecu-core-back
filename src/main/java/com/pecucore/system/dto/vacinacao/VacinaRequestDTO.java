package com.pecucore.system.dto.vacinacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VacinaRequestDTO(
        @NotBlank(message = "O nome da vacina precisa ser informado!!")
        @Size(max = 100, message = "O nome da vacina deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O fabricante da vacina precisa ser informado!")
        @Size(max = 100, message = "O fabricante deve ter no máximo 100 caracteres")
        String fabricante,

        @NotNull(message = "A carência da vacina precisa ser informada.")
        @PositiveOrZero(message = "A carência não pode ser negativa")
        Integer carenciaDias,

        @Positive(message = "O intervalo entre doses deve ser maior que zero.")
        Integer intervaloDoseDias
) {
}
