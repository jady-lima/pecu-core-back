package com.Pecucore.system.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MedicamentoUpdateRequestDTO(

        @NotBlank(message = "O nome do medicamento precisa ser informado!")
        @Size(max = 100, message = "O nome do medicamento deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O motivo do medicamento precisa ser informado!")
        @Size(max = 100, message = "O motivo do medicamento deve ter no máximo 100 caracteres")
        String motivo,

        @NotNull(message = "A dosagem do medicamento precisa ser informada.")
        @Positive(message = "A dosagem deve ser maior que zero.")
        Double dosagem,

        @NotNull(message = "A carência do medicamento precisa ser informada.")
        @PositiveOrZero(message = "A carência não pode ser negativa")
        Integer carenciaDias,

        @NotNull(message = "A data da aplicação precisa ser informada.")
        @PastOrPresent(message = "A data da aplicação não pode ser futura.")
        LocalDate data,

        @Positive(message = "O valor deve ser maior que zero.")
        Double valor
) {
}
