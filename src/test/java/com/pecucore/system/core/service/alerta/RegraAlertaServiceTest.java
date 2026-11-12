package com.pecucore.system.core.service.alerta;

import com.pecucore.system.core.exception.AcessoNegadoException;
import com.pecucore.system.core.exception.RecursoNaoEncontradoException;
import com.pecucore.system.core.exception.RegraNegocioException;
import com.pecucore.system.core.model.*;
import com.pecucore.system.core.repository.PropriedadeRepository;
import com.pecucore.system.core.repository.RegraAlertaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegraAlertaServiceTest {

    private static final Long PROPRIEDADE_ID = 1L;

    @Mock
    private RegraAlertaRepository regraAlertaRepository;

    @Mock
    private PropriedadeRepository propriedadeRepository;

    @InjectMocks
    private RegraAlertaService regraAlertaService;

    private Propriedade propriedade;
    private Usuario produtor;
    private Usuario veterinario;

    @BeforeEach
    void setUp() {
        produtor = usuario(100L, Perfil.PRODUTOR);
        veterinario = usuario(200L, Perfil.VETERINARIO);

        propriedade = mock(Propriedade.class);
        lenient().when(propriedade.getUsuario()).thenReturn(produtor);
        lenient().when(propriedade.getVeterinarios()).thenReturn(Set.of(veterinario));

        lenient().when(propriedadeRepository.findById(PROPRIEDADE_ID))
                .thenReturn(Optional.of(propriedade));
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void produtorDaPropriedadeCriaRegraAtiva() {
        autenticar(produtor);
        when(regraAlertaRepository.save(any(RegraAlerta.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        RegraAlerta regra = regraAlertaService.createRegraAlerta(
                PROPRIEDADE_ID, TipoAlerta.GMD_ABAIXO_META, 1.2);

        assertEquals(TipoAlerta.GMD_ABAIXO_META, regra.getTipo());
        assertEquals(1.2, regra.getParametro());
        assertTrue(regra.isAtiva());
        assertSame(propriedade, regra.getPropriedade());
    }

    @ParameterizedTest(name = "{0} com parametro {1} deve ser recusada")
    @CsvSource({
            "GMD_ABAIXO_META, 0",
            "CUSTO_ACIMA, -5",
            "PESAGEM_ATRASADA, 30.5",
            "QUEDA_PESO, 10"
    })
    void deveRecusarRegraInvalida(TipoAlerta tipo, double parametro) {
        assertThrows(RegraNegocioException.class,
                () -> regraAlertaService.createRegraAlerta(PROPRIEDADE_ID, tipo, parametro));

        verify(regraAlertaRepository, never()).save(any());
    }

    @Test
    void deveFalharQuandoPropriedadeNaoExiste() {
        when(propriedadeRepository.findById(PROPRIEDADE_ID)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> regraAlertaService.createRegraAlerta(
                        PROPRIEDADE_ID, TipoAlerta.GMD_ABAIXO_META, 1.2));
    }

    @Test
    void veterinarioNaoPodeCriarRegraFinanceira() {
        autenticar(veterinario);

        assertThrows(AcessoNegadoException.class,
                () -> regraAlertaService.createRegraAlerta(
                        PROPRIEDADE_ID, TipoAlerta.CUSTO_ACIMA, 500));

        verify(regraAlertaRepository, never()).save(any());
    }

    @Test
    void produtorDeOutraPropriedadeNaoPodeCriarRegra() {
        autenticar(usuario(300L, Perfil.PRODUTOR));

        assertThrows(AcessoNegadoException.class,
                () -> regraAlertaService.createRegraAlerta(
                        PROPRIEDADE_ID, TipoAlerta.GMD_ABAIXO_META, 1.2));

        verify(regraAlertaRepository, never()).save(any());
    }

    // ---------- Auxiliares ----------

    private Usuario usuario(Long id, Perfil perfil) {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(id);
        lenient().when(usuario.getPerfil()).thenReturn(perfil);
        return usuario;
    }

    private void autenticar(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }
}