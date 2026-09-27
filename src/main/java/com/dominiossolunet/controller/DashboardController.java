package com.dominiossolunet.controller;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/gestion")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        List<Dominio> dominios = dashboardService.obtenerDominios();

        model.addAttribute("totalDominios", dashboardService.totalDominios(dominios));
        model.addAttribute("dominiosActivos", dashboardService.dominiosActivos(dominios));
        model.addAttribute("pendientesRenovacion", dashboardService.pendientesRenovacion(dominios));
        model.addAttribute("renovacionesRealizadas", dashboardService.renovacionesRealizadas(dominios));
        model.addAttribute("renovacionesRechazadas", dashboardService.renovacionesRechazadas(dominios));
        model.addAttribute("expirados", dashboardService.expirados(dominios));
        model.addAttribute("expiranEn30Dias", dashboardService.expiranEn30Dias(dominios));
        model.addAttribute("proximasExpiraciones", dashboardService.obtenerProximasExpiraciones(dominios));
        model.addAttribute("dominiosPorRegistrador", dashboardService.dominiosPorRegistrador(dominios));

        return "gestion/dashboard";
    }
}
