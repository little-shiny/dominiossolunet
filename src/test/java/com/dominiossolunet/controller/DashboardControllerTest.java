package com.dominiossolunet.controller;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private Model model;

    @InjectMocks
    private DashboardController dashboardController;

    private List<Dominio> dominios;

    @BeforeEach
    void setUp() {
        dominios = List.of(
                new Dominio(),
                new Dominio()
        );
    }

    @Test
    void dashboard_devuelveVistaDashboard() {

        configurarServicio();

        String vista = dashboardController.dashboard(model);

        assertThat(vista)
                .isEqualTo("gestion/dashboard");
    }

    @Test
    void dashboard_obtieneLosDominiosDelServicio() {

        configurarServicio();

        dashboardController.dashboard(model);

        verify(dashboardService)
                .obtenerDominios();
    }

    @Test
    void dashboard_calculaYAnadeTodosLosDatosAlModelo() {

        configurarServicio();

        dashboardController.dashboard(model);

        verify(dashboardService)
                .totalDominios(dominios);

        verify(dashboardService)
                .dominiosActivos(dominios);

        verify(dashboardService)
                .pendientesRenovacion(dominios);

        verify(dashboardService)
                .renovacionesRealizadas(dominios);

        verify(dashboardService)
                .renovacionesRechazadas(dominios);

        verify(dashboardService)
                .expirados(dominios);

        verify(dashboardService)
                .expiranEn30Dias(dominios);

        verify(dashboardService)
                .obtenerProximasExpiraciones(dominios);

        verify(dashboardService)
                .dominiosPorRegistrador(dominios);
    }

    @Test
    void dashboard_anadeLosValoresCorrectosAlModelo() {

        List<Dominio> proximasExpiraciones =
                List.of(dominios.get(0));

        Map<Registrador, Long> dominiosPorRegistrador =
                Map.of(
                        Registrador.DOMITECA,
                        2L
                );

        when(dashboardService.obtenerDominios())
                .thenReturn(dominios);

        when(dashboardService.totalDominios(dominios))
                .thenReturn(2L);

        when(dashboardService.dominiosActivos(dominios))
                .thenReturn(1L);

        when(dashboardService.pendientesRenovacion(dominios))
                .thenReturn(1L);

        when(dashboardService.renovacionesRealizadas(dominios))
                .thenReturn(0L);

        when(dashboardService.renovacionesRechazadas(dominios))
                .thenReturn(0L);

        when(dashboardService.expirados(dominios))
                .thenReturn(0L);

        when(dashboardService.expiranEn30Dias(dominios))
                .thenReturn(1L);

        when(dashboardService.obtenerProximasExpiraciones(dominios))
                .thenReturn(proximasExpiraciones);

        when(dashboardService.dominiosPorRegistrador(dominios))
                .thenReturn(dominiosPorRegistrador);

        dashboardController.dashboard(model);

        verify(model).addAttribute(
                "totalDominios",
                2L
        );

        verify(model).addAttribute(
                "dominiosActivos",
                1L
        );

        verify(model).addAttribute(
                "pendientesRenovacion",
                1L
        );

        verify(model).addAttribute(
                "renovacionesRealizadas",
                0L
        );

        verify(model).addAttribute(
                "renovacionesRechazadas",
                0L
        );

        verify(model).addAttribute(
                "expirados",
                0L
        );

        verify(model).addAttribute(
                "expiranEn30Dias",
                1L
        );

        verify(model).addAttribute(
                "proximasExpiraciones",
                proximasExpiraciones
        );

        verify(model).addAttribute(
                "dominiosPorRegistrador",
                dominiosPorRegistrador
        );
    }

    @Test
    void dashboard_conListaVacia_anadeValoresVaciosYDevuelveVista() {

        List<Dominio> vacio = List.of();

        when(dashboardService.obtenerDominios())
                .thenReturn(vacio);

        when(dashboardService.totalDominios(vacio))
                .thenReturn(0L);

        when(dashboardService.dominiosActivos(vacio))
                .thenReturn(0L);

        when(dashboardService.pendientesRenovacion(vacio))
                .thenReturn(0L);

        when(dashboardService.renovacionesRealizadas(vacio))
                .thenReturn(0L);

        when(dashboardService.renovacionesRechazadas(vacio))
                .thenReturn(0L);

        when(dashboardService.expirados(vacio))
                .thenReturn(0L);

        when(dashboardService.expiranEn30Dias(vacio))
                .thenReturn(0L);

        when(dashboardService.obtenerProximasExpiraciones(vacio))
                .thenReturn(List.of());

        when(dashboardService.dominiosPorRegistrador(vacio))
                .thenReturn(Map.of());

        String vista =
                dashboardController.dashboard(model);

        assertThat(vista)
                .isEqualTo("gestion/dashboard");

        verify(model).addAttribute(
                "totalDominios",
                0L
        );

        verify(model).addAttribute(
                "dominiosActivos",
                0L
        );

        verify(model).addAttribute(
                "pendientesRenovacion",
                0L
        );

        verify(model).addAttribute(
                "renovacionesRealizadas",
                0L
        );

        verify(model).addAttribute(
                "renovacionesRechazadas",
                0L
        );

        verify(model).addAttribute(
                "expirados",
                0L
        );

        verify(model).addAttribute(
                "expiranEn30Dias",
                0L
        );

        verify(model).addAttribute(
                "proximasExpiraciones",
                List.of()
        );

        verify(model).addAttribute(
                "dominiosPorRegistrador",
                Map.of()
        );
    }

    private void configurarServicio() {

        when(dashboardService.obtenerDominios())
                .thenReturn(dominios);

        when(dashboardService.totalDominios(dominios))
                .thenReturn(2L);

        when(dashboardService.dominiosActivos(dominios))
                .thenReturn(1L);

        when(dashboardService.pendientesRenovacion(dominios))
                .thenReturn(1L);

        when(dashboardService.renovacionesRealizadas(dominios))
                .thenReturn(0L);

        when(dashboardService.renovacionesRechazadas(dominios))
                .thenReturn(0L);

        when(dashboardService.expirados(dominios))
                .thenReturn(0L);

        when(dashboardService.expiranEn30Dias(dominios))
                .thenReturn(1L);

        when(dashboardService.obtenerProximasExpiraciones(dominios))
                .thenReturn(List.of());

        when(dashboardService.dominiosPorRegistrador(dominios))
                .thenReturn(Map.of());
    }
}
