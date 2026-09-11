package com.Pecucore.system.dto;

import com.Pecucore.system.model.Lote;
import java.time.LocalDate;

public record LoteResponseDTO( Long id, int numero, String finalidade,int capacidade,LocalDate dataCriacao, Long propriedadeId){
 public LoteResponseDTO(Lote lote){
     this( lote.getId(), lote.getNumero(), lote.getFinalidade(), lote.getCapacidade(),lote.getDataCriacao(), lote.getPropriedade().getId());
 }
}

