package com.pecucore.system.core.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "agrocore")
public record AgroCoreProperties(@NotNull Aplicacao aplicacao) {

    public enum Aplicacao {
        BOVINOS,
        OVINOS,
        EQUINOS
    }
}