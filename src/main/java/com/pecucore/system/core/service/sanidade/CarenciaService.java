package com.pecucore.system.core.service.sanidade;

import com.pecucore.system.core.exception.ConflitoNegocioException;
import com.pecucore.system.core.extensao.sanitario.PoliticaRestricaoSanitaria;
import com.pecucore.system.core.model.StatusAnimal;
import com.pecucore.system.core.service.medicamento.MedicamentoConsultaService;
import com.pecucore.system.core.service.vacinacao.VacinacaoConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class CarenciaService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private VacinacaoConsultaService vacinacaoConsultaService;

    @Autowired
    private MedicamentoConsultaService medicamentoConsultaService;

    @Autowired
    private PoliticaRestricaoSanitaria politicaRestricaoSanitaria;

    public boolean estaEmCarencia(Long animalId) {
        return getDataFimCarencia(animalId).isPresent();
    }

    public Optional<LocalDate> getDataFimCarencia(Long animalId) {
        LocalDate hoje = LocalDate.now();

        return Stream.of(
                        vacinacaoConsultaService.getDataFimCarenciaVigente(animalId, hoje),
                        medicamentoConsultaService.getDataFimCarenciaVigente(animalId, hoje)
                )
                .flatMap(Optional::stream)
                .max(LocalDate::compareTo);
    }

    public void validarMudancaStatus(Long animalId, StatusAnimal novoStatus) {
        if (!politicaRestricaoSanitaria.getStatusBloqueadosNaCarencia().contains(novoStatus)) {
            return;
        }

        getDataFimCarencia(animalId).ifPresent(dataFim -> {
            throw new ConflitoNegocioException(
                    "O animal está em período de carência até " + dataFim.format(DATA)
                            + " e não pode ter o status " + novoStatus + "."
            );
        });
    }
}
