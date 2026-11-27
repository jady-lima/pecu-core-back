package com.Pecucore.system.ovinos.service.sanidade;





import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitaria;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.model.Vacina;
import com.pecucore.system.core.model.Vacinacao;
import com.pecucore.system.core.service.vacinacao.VacinaService;
import com.pecucore.system.core.service.vacinacao.VacinacaoConsultaService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class RestricaoSanitariaOvinos implements PoliticaRestricaoSanitaria {
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
