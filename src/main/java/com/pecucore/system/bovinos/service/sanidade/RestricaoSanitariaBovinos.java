package com.pecucore.system.bovinos.service.sanidade;

import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitaria;
import com.pecucore.system.core.model.StatusAnimal;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Component
public class RestricaoSanitariaBovinos implements PoliticaRestricaoSanitaria {

    private static final Set<StatusAnimal> STATUS_BLOQUEADOS = EnumSet.of(StatusAnimal.VENDIDO, StatusAnimal.ABATIDO);

    @Override
    public LocalDate calcularFimCarencia(LocalDate dataAplicacao, int carenciaDias) {
        return dataAplicacao.plusDays(carenciaDias);
    }

    @Override
    public Set<StatusAnimal> getStatusBloqueadosNaCarencia() {
        return STATUS_BLOQUEADOS;
    }
}
