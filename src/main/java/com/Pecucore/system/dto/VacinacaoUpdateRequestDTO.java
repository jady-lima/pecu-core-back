package com.Pecucore.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record VacinacaoUpdateRequestDTO(
        @NotNull(message = "A vacina precisa ser informada!")
        Long vacinaId,

        @NotNull(message = "O aplicador da vacina precisa ser informado.")
        Long aplicadorId,

        @NotNull(message = "A data da aplicação precisa ser informada.")
        @PastOrPresent(message = "A data da aplicação não pode ser uma data futura")
        LocalDate data,

        @NotBlank(message = "O lote da vacina precisa ser informado!")
        @Size(max = 50, message = "O lote da vacina deve ter no máximo 50 caracteres")
        String loteVacina,

        @NotNull(message = "A dose aplicada precisa ser informada!")
        @Positive(message = "A dose aplicada deve ser maior que zero")
        Double doseAplicada,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes,

        @Positive(message = "O valor deve ser maior que zero.")
        Double valor
) {
}
