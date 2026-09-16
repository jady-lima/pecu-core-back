package com.Pecucore.system.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record AnimalRequestDTO(

        @NotNull(message = "O brinco precisa ser informado!!")
        Integer brinco,

        @NotNull(message = "A data de nascimento do animal precisa ser informada!!")
        LocalDate dataNascimento,

        @NotNull(message = "O peso precisa ser informado!!")
        @Positive(message = "O peso deve ser maior que zero")
        Double pesoAtual,

        @NotNull(message = "O sexo precisa ser informado!!")
        String sexo,

        @NotNull(message = "O lote precisa ser informado!!")
        Long loteId

) {

}