package com.dominiossolunet.controller;

import com.dominiossolunet.service.GestionDominioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/gestion")
public class GestionDominioController {

    private final GestionDominioService gestionDominioService;

    public GestionDominioController(
            GestionDominioService gestionDominioService) {

        this.gestionDominioService = gestionDominioService;
    }

    @PostMapping("/dominios/{id}/renovar")
    public String marcarComoRenovado(
            @PathVariable int id) {

        gestionDominioService.marcarComoRenovado(id);

        return "redirect:/gestion/dominios";
    }

    @GetMapping("/dominios")
    public String listarDominios(Model model) {

        model.addAttribute(
                "dominios",
                gestionDominioService.obtenerTodosLosDominios()
        );

        return "gestion/dominios";
    }
}