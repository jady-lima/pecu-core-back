package com.pecucore.system.core.dto.custo;

import com.pecucore.system.core.model.TipoCusto;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CustoRequestDTO(

        @NotNull(message = "O tipo do custo precisa ser informado.")
        TipoCusto tipo,

        @NotNull(message = "O valor precisa ser informado.")
        @Positive(message = "O valor deve ser maior que zero.")
        Double valor,

        @NotNull(message = "A data precisa ser informada.")
        @PastOrPresent(message = "A data não pode ser futura.")
        LocalDate data,

        Long animalId,

        Long loteId,

        Long propriedadeId
) {
}