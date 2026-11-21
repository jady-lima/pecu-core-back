package com.pecucore.system.core.extensao.sanitario;

import com.pecucore.system.core.extensao.PontoFlexivel;
import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.model.StatusAnimal;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@PontoFlexivel
public interface PoliticaRestricaoSanitaria {

    LocalDate calcularFimCarencia(LocalDate dataAplicacao, int carenciaDias);

    Set<StatusAnimal> getStatusBloqueadosNaCarencia();

    /**
     * Restrições sanitárias da aplicação, além da carência, que impedem o animal de mudar para o status.
     * Cada item é uma mensagem para o usuário; lista vazia significa nenhuma restrição.
     * Padrão: nenhuma restrição (a aplicação só sobrescreve se precisar).
     */
    default List<String> getRestricoesAdicionais(Animal animal, StatusAnimal novoStatus, LocalDate dataReferencia) {
        return List.of();
    }
}