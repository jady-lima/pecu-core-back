package com.pecucore.system.ia;

import com.pecucore.system.dto.analise.RelatorioAnaliseDTO;

public record ResultadoAnaliseIA(RelatorioAnaliseDTO relatorio, String conteudoBruto, String modelo) {
}
