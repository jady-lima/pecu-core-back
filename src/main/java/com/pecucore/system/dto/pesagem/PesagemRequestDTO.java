package com.pecucore.system.dto.pesagem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record PesagemRequestDTO(

        @NotNull(message = "O brinco do animal precisa ser informado!!")
        Integer brinco,

        @NotNull(message = "O peso precisa ser informado!!")
        @Positive(message = "O peso deve ser maior que zero")
        Double pesoAtual,

        @NotNull(message = "A data da pesagem precisa ser informada!!")
        @PastOrPresent(message = "A data da pesagem não pode ser uma data futura")
        LocalDate dataPesagem

) {
}
