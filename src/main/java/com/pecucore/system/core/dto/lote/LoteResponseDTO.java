package com.pecucore.system.core.dto.lote;

import com.pecucore.system.bovinos.agrupamento.FinalidadeLote;
import com.pecucore.system.core.model.Lote;
import java.time.LocalDate;

public record LoteResponseDTO(Long id, int numero, FinalidadeLote finalidade, int capacidade, LocalDate dataCriacao, Long propriedadeId, Double gmdMeta){
 public LoteResponseDTO(Lote lote){
     this( lote.getId(), lote.getNumero(), lote.getFinalidade(), lote.getCapacidade(),lote.getDataCriacao(), lote.getPropriedade().getId(), lote.getGmdMeta());
 }
}
