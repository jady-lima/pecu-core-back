package com.pecucore.system.core.extensao.aplicacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contrato do ponto flexível DescricaoAplicacao.
 * Uso no núcleo: OpenAPIConfig (descrição do Swagger) e PromptAnaliseBuilder ({SISTEMA} e {ESPECIE} no prompt da IA).
 */
public abstract class DescricaoAplicacaoContrato {

    protected DescricaoAplicacao descricao;

    protected abstract DescricaoAplicacao criarDescricao();

    @BeforeEach
    public void prepararDescricao() {
        descricao = criarDescricao();
    }

    @Test
    @DisplayName("Nome do sistema está preenchido (Swagger e análise de IA)")
    public void nomeSistemaPreenchido() {
        assertFalse(descricao.getNomeSistema() == null || descricao.getNomeSistema().isBlank());
    }

    @Test
    @DisplayName("Espécie está preenchida (Swagger e análise de IA)")
    public void especiePreenchida() {
        assertFalse(descricao.getEspecie() == null || descricao.getEspecie().isBlank());
    }
}