package com.Pecucore.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record PesagemRequestDTO(

        @NotNull(message = "O animal precisa ser informado!!")
        Long animalId,

        @NotNull(message = "O peso precisa ser informado!!")
        @Positive(message = "O peso deve ser maior que zero")
        Double pesoAtual,

        @NotNull(message = "A data da pesagem precisa ser informada!!")
        LocalDate dataPesagem

) {
}
