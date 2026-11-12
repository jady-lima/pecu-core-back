package com.pecucore.system.core.ia.parser;

import com.pecucore.system.core.dto.analise.RelatorioAnaliseDTO;
import com.pecucore.system.core.exception.ConteudoAnaliseInvalidoException;
import com.pecucore.system.core.exception.ServicoIAIndisponivelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class RelatorioAnaliseParser {

    @Autowired
    private JsonMapper jsonMapper;

    public RelatorioAnaliseDTO converterRespostaIA(String json) {
        try {
            RelatorioAnaliseDTO relatorio = jsonMapper.readValue(json, RelatorioAnaliseDTO.class);

            if (!isCompleto(relatorio)) {
                throw new ServicoIAIndisponivelException("O serviço de IA retornou um relatório incompleto. Tente novamente.");
            }

            return relatorio;
        } catch (JacksonException exception) {
            throw new ServicoIAIndisponivelException("O serviço de IA retornou um relatório em formato inválido. Tente novamente.", exception);
        }
    }

    public RelatorioAnaliseDTO converterConteudoSalvo(String json) {
        try {
            RelatorioAnaliseDTO relatorio = jsonMapper.readValue(json, RelatorioAnaliseDTO.class);

            if (!isCompleto(relatorio)) {
                throw new ConteudoAnaliseInvalidoException("O conteúdo salvo da análise está incompleto.");
            }

            return relatorio;
        } catch (JacksonException exception) {
            throw new ConteudoAnaliseInvalidoException("O conteúdo salvo da análise está em formato inválido.", exception);
        }
    }

    private boolean isCompleto(RelatorioAnaliseDTO relatorio) {
        return relatorio != null && relatorio.situacaoGeral() != null && relatorio.resumo() != null;
    }
}
