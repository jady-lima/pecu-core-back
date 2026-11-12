package com.pecucore.system.core.dto.erro;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Formato padrão de erro da API")
public record ErroResponseDTO(
        @Schema(description = "Código HTTP", example = "404")
        int status,

        @Schema(description = "Nome do status HTTP", example = "NOT_FOUND")
        String erro,

        @Schema(description = "Mensagem para exibir ao usuário", example = "Animal não encontrado")
        String mensagem,

        @Schema(description = "Endereço da requisição que falhou", example = "/animais/99")
        String caminho,

        @Schema(description = "Momento em que o erro ocorreu")
        LocalDateTime dataHora,

        @Schema(description = "Erros por campo, preenchido quando a validação falha")
        List<CampoInvalidoDTO> campos
) {

    public static ErroResponseDTO de(HttpStatus status, String mensagem, String caminho) {
        return de(status, mensagem, caminho, List.of());
    }

    public static ErroResponseDTO de(
            HttpStatus status,
            String mensagem,
            String caminho,
            List<CampoInvalidoDTO> campos
    ) {
        return new ErroResponseDTO(
                status.value(),
                status.name(),
                mensagem,
                caminho,
                LocalDateTime.now(),
                campos
        );
    }
}