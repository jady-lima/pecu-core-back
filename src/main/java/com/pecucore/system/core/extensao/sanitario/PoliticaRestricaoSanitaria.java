package com.pecucore.system.core.extensao.sanitario;

import com.pecucore.system.core.extensao.PontoFlexivel;
import com.pecucore.system.core.model.StatusAnimal;

import java.time.LocalDate;
import java.util.Set;

@PontoFlexivel
public interface PoliticaRestricaoSanitaria {

    LocalDate calcularFimCarencia(LocalDate dataAplicacao, int carenciaDias);

    Set<StatusAnimal> getStatusBloqueadosNaCarencia();
}
