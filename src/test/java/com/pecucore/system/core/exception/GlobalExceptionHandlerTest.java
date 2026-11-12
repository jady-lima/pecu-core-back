package com.pecucore.system.core.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @ParameterizedTest(name = "{0} deve responder {1}")
    @CsvSource({
            "/teste/nao-encontrado,      404, NOT_FOUND,             Animal nao encontrado",
            "/teste/conflito,            409, CONFLICT,              Brinco ja cadastrado",
            "/teste/regra,               400, BAD_REQUEST,           Parametro invalido",
            "/teste/acesso,              403, FORBIDDEN,             Sem acesso a propriedade",
            "/teste/dados-insuficientes, 422, UNPROCESSABLE_CONTENT, Pesagens insuficientes",
            "/teste/ia,                  503, SERVICE_UNAVAILABLE,   IA fora do ar"
    })
    void deveResponderNoFormatoPadrao(String caminho, int status, String erro, String mensagem)
            throws Exception {

        mockMvc.perform(get(caminho))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.erro").value(erro))
                .andExpect(jsonPath("$.mensagem").value(mensagem))
                .andExpect(jsonPath("$.caminho").value(caminho))
                .andExpect(jsonPath("$.dataHora").exists())
                .andExpect(jsonPath("$.campos", hasSize(0)));
    }

    @Test
    void naoDeveExporDetalhesDeErroInesperado() throws Exception {
        mockMvc.perform(get("/teste/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.erro").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.mensagem", containsString("Tente novamente")))
                .andExpect(jsonPath("$.mensagem", not(containsString("detalhe interno"))));
    }

    @Test
    void deveListarTodosOsCamposInvalidos() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.mensagem", not(emptyOrNullString())))
                .andExpect(jsonPath("$.campos", hasSize(2)))
                .andExpect(jsonPath("$.campos[*].campo", containsInAnyOrder("nome", "idade")));
    }

    @Test
    void deveRecusarCorpoMalFormatado() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ nome: "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("BAD_REQUEST"));
    }

    @Test
    void deveRecusarParametroComTipoErrado() throws Exception {
        mockMvc.perform(get("/teste/numero/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("'id'")));
    }

    @Test
    void deveExigirParametroObrigatorio() throws Exception {
        mockMvc.perform(get("/teste/parametro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("'nome'")));
    }

    @Test
    void deveRecusarMetodoNaoSuportado() throws Exception {
        mockMvc.perform(delete("/teste/parametro"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.erro").value("METHOD_NOT_ALLOWED"));
    }

    // ---------- Controller usado só neste teste ----------

    @RestController
    static class ControllerDeTeste {

        record Dados(
                @NotBlank(message = "O nome e obrigatorio") String nome,
                @NotNull(message = "A idade e obrigatoria") Integer idade
        ) {
        }

        @GetMapping("/teste/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("Animal nao encontrado");
        }

        @GetMapping("/teste/conflito")
        void conflito() {
            throw new ConflitoNegocioException("Brinco ja cadastrado");
        }

        @GetMapping("/teste/regra")
        void regra() {
            throw new RegraNegocioException("Parametro invalido");
        }

        @GetMapping("/teste/acesso")
        void acesso() {
            throw new AcessoNegadoException("Sem acesso a propriedade");
        }

        @GetMapping("/teste/dados-insuficientes")
        void dadosInsuficientes() {
            throw new DadosInsuficientesException("Pesagens insuficientes");
        }

        @GetMapping("/teste/ia")
        void ia() {
            throw new ServicoIAIndisponivelException("IA fora do ar");
        }

        @GetMapping("/teste/inesperado")
        void inesperado() {
            throw new IllegalStateException("detalhe interno");
        }

        @PostMapping("/teste/validacao")
        void validacao(@RequestBody @Valid Dados dados) {
        }

        @GetMapping("/teste/numero/{id}")
        void numero(@PathVariable Long id) {
        }

        @GetMapping("/teste/parametro")
        void parametro(@RequestParam String nome) {
        }
    }
}