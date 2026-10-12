package com.Pecucore.system;

import com.Pecucore.system.model.Pesagem;
import com.Pecucore.system.service.GmdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GmdServiceTest {

    private GmdService gmdService;

    @BeforeEach
    void setUp() {
        gmdService = new GmdService();
    }

    @Test
    void deveCalcularGmdEntreDuasPesagens() {

        Pesagem primeira = new Pesagem();
        primeira.setPesoKg(300);
        primeira.setData(LocalDate.of(2026, 9, 1));

        Pesagem ultima = new Pesagem();
        ultima.setPesoKg(340);
        ultima.setData(LocalDate.of(2026, 9, 30));

        Double gmd = gmdService.calculateGmdHistorico(
                primeira,
                ultima
        );

        assertEquals(40.0 / 29.0, gmd, 0.0001);
    }

    @Test
    void deveRetornarNullQuandoNaoExistemDuasPesagens() {

        Double gmd = gmdService.calculateGmdHistorico(
                null,
                null
        );

        assertNull(gmd);
    }

    @Test
    void deveRetornarNullQuandoAsPesagensEstaoNoMesmoDia() {

        Pesagem primeira = new Pesagem();
        primeira.setPesoKg(300);
        primeira.setData(LocalDate.of(2026, 9, 1));

        Pesagem ultima = new Pesagem();
        ultima.setPesoKg(310);
        ultima.setData(LocalDate.of(2026, 9, 1));

        Double gmd = gmdService.calculateGmdHistorico(
                primeira,
                ultima
        );

        assertNull(gmd);
    }

    @Test
    void devePermitirGmdNegativoQuandoAnimalPerdePeso() {

        Pesagem primeira = new Pesagem();
        primeira.setPesoKg(340);
        primeira.setData(LocalDate.of(2026, 9, 1));

        Pesagem ultima = new Pesagem();
        ultima.setPesoKg(300);
        ultima.setData(LocalDate.of(2026, 9, 30));

        Double gmd = gmdService.calculateGmdHistorico(
                primeira,
                ultima
        );

        assertEquals(-40.0 / 29.0, gmd, 0.0001);
    }
    @Test
    void deveRetornarNullQuandoAsPesagensEstaoForaDeOrdem() {

        Pesagem primeira = new Pesagem();
        primeira.setPesoKg(300);
        primeira.setData(LocalDate.of(2026, 9, 30));

        Pesagem ultima = new Pesagem();
        ultima.setPesoKg(340);
        ultima.setData(LocalDate.of(2026, 9, 1));

        Double gmd = gmdService.calculateGmdHistorico(
                primeira,
                ultima
        );

        assertNull(gmd);
    }

}