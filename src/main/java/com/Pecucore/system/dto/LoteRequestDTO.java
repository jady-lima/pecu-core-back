package com.Pecucore.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record LoteRequestDTO(
        @NotNull(message = "O lote precisa ter um número cadastrado!!")
        Integer numero,

        @NotBlank(message = "A finalidade do lote precisa ser informada!!")
        String finalidade,

        @NotNull(message = "A capacidade do lote precisa ser informada!!")
        @Positive(message = "A capacidade do lote precisa ser maior do que zero!!")
        Integer capacidade,


        @NotNull(message = "A data de criação precisa ser informada!!")
        LocalDate dataCriacao,


        @NotNull(message = "A propriedade precisa ser informada!!")
        Long propriedadeId
) {

}




