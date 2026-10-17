package com.Pecucore.system.ia;

import com.Pecucore.system.dto.analise.RelatorioAnaliseDTO;

public record ResultadoAnaliseIA(RelatorioAnaliseDTO relatorio, String conteudoBruto, String modelo) {
}
