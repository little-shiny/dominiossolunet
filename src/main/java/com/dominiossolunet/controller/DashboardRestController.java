package com.dominiossolunet.controller;

import com.dominiossolunet.dto.DashboardResponse;
import com.dominiossolunet.dto.DominioProximaExpiracionResponse;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private final DashboardService dashboardService;

    public DashboardRestController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse obtenerDashboard() {
        List<Dominio> dominios = dashboardService.obtenerDominios();

        List<DominioProximaExpiracionResponse> proximasExpiraciones =
                dashboardService.obtenerProximasExpiraciones(dominios).stream()
                        .map(dominio -> new DominioProximaExpiracionResponse(
                                dominio.getId(),
                                dominio.getNombreDominio(),
                                dominio.getCliente().getNombre(),
                                dominio.getFechaExpiracion(),
                                dominio.getEstadoRenovacion() == null
                                        ? null
                                        : dominio.getEstadoRenovacion().name()
                        ))
                        .toList();

        Map<String, Long> dominiosPorRegistrador =
                dashboardService.dominiosPorRegistrador(dominios).entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().name(),
                                Map.Entry::getValue
                        ));

        return new DashboardResponse(
                dashboardService.totalDominios(dominios),
                dashboardService.dominiosActivos(dominios),
                dashboardService.pendientesRenovacion(dominios),
                dashboardService.renovacionesRealizadas(dominios),
                dashboardService.renovacionesRechazadas(dominios),
                dashboardService.expirados(dominios),
                dashboardService.expiranEn30Dias(dominios),
                proximasExpiraciones,
                dominiosPorRegistrador
        );
    }
}