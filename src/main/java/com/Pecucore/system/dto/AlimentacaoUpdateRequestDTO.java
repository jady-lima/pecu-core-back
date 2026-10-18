package com.Pecucore.system.dto;

import com.Pecucore.system.model.TipoAlimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record AlimentacaoUpdateRequestDTO(
        @NotNull(message = "O tipo de alimentação precisa ser informado.")
        TipoAlimentacao tipo,

        @NotNull(message = "A quantidade precisa ser informada.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        Double quantidade,

        @NotNull(message = "A data da alimentação precisa ser informada.")
        @PastOrPresent(message = "A data da alimentação não pode ser uma data futura")
        LocalDate data,

        @Positive(message = "O valor deve ser maior que zero.")
        Double valor
) {
}
