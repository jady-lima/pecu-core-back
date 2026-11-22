package com.pecucore.system.equinos.service.sanidade;

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

/**
 * Restrições sanitárias dos equinos para a venda (status VENDIDO):
 * - carência vigente de vacina ou medicamento, como nos bovinos;
 * - vacinas e exames marcados como exigidos para o trânsito (Vacina.exigidaParaTransito, ex.: exame de AIE):
 *   de cada um, o animal precisa ter um registro em dia. A validade é o intervalo cadastrado na vacina
 *   (ex.: AIE = 60 dias a partir da coleta).
 * Quais exames são exigidos varia por estado e muda com o tempo (ex.: mormo, após a Portaria MAPA nº 593/2023);
 * por isso a lista é cadastrada pelo usuário, e não fixada aqui.
 * Limitação: o sistema não registra o resultado do exame; só exames negativos devem ser lançados.
 */
@Component
public class RestricaoSanitariaEquinos implements PoliticaRestricaoSanitaria {

    private static final Set<StatusAnimal> STATUS_BLOQUEADOS = EnumSet.of(StatusAnimal.VENDIDO);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VacinaService vacinaService;
    private final VacinacaoConsultaService vacinacaoConsultaService;

    public RestricaoSanitariaEquinos(VacinaService vacinaService, VacinacaoConsultaService vacinacaoConsultaService) {
        this.vacinaService = vacinaService;
        this.vacinacaoConsultaService = vacinacaoConsultaService;
    }

    @Override
    public LocalDate calcularFimCarencia(LocalDate dataAplicacao, int carenciaDias) {
        return dataAplicacao.plusDays(carenciaDias);
    }

    @Override
    public Set<StatusAnimal> getStatusBloqueadosNaCarencia() {
        return STATUS_BLOQUEADOS;
    }

    @Override
    public List<String> getRestricoesAdicionais(Animal animal, StatusAnimal novoStatus, LocalDate dataReferencia) {
        if (!STATUS_BLOQUEADOS.contains(novoStatus)) {
            return List.of();
        }

        List<Vacina> exigidas = vacinaService.getAllVacinasExigidasParaTransito();

        if (exigidas.isEmpty()) {
            return List.of();
        }

        Map<Long, Vacinacao> ultimaPorVacina = vacinacaoConsultaService
                .getUltimasVacinacoesByAnimalId(animal.getId())
                .stream()
                .collect(Collectors.toMap(vacinacao -> vacinacao.getVacina().getId(), Function.identity()));

        List<String> restricoes = new ArrayList<>();

        for (Vacina vacina : exigidas) {
            Vacinacao ultima = ultimaPorVacina.get(vacina.getId());

            if (ultima == null) {
                restricoes.add(vacina.getNome() + ": sem registro");
            } else if (ultima.getDataProximaDose() == null) {
                restricoes.add(vacina.getNome() + ": registro sem validade");
            } else if (ultima.getDataProximaDose().isBefore(dataReferencia)) {
                restricoes.add(vacina.getNome() + ": vencido em " + ultima.getDataProximaDose().format(DATA));
            }
        }

        return restricoes;
    }
}