package com.pecucore.system.evento;

import java.time.LocalDate;

public record PesoAnimalAlteradoEvento(Long animalId, double pesoKg, LocalDate data) {}
