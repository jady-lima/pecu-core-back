package com.pecucore.system.core.ia;

import com.pecucore.system.core.dto.analise.RelatorioAnaliseDTO;

public record ResultadoAnaliseIA(RelatorioAnaliseDTO relatorio, String conteudoBruto, String modelo) {
}
