package com.pecucore.system.core.service.alerta.verificador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FormatadorAlerta {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatadorAlerta() {
    }

    public static String moeda(double valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }

    public static String gmd(double kgPorDia) {
        return String.format(PT_BR, "%.2f kg/dia", kgPorDia);
    }

    public static String data(LocalDate data) {
        return data.format(DATA);
    }
}
