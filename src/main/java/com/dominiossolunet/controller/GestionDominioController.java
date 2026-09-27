package com.dominiossolunet.controller;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.service.FacturacionService;
import com.dominiossolunet.service.GestionDominioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/gestion")
public class GestionDominioController {

    private final GestionDominioService gestionDominioService;
    private final FacturacionService facturacionService;

    public GestionDominioController(GestionDominioService gestionDominioService, FacturacionService facturacionService) {
        this.gestionDominioService = gestionDominioService;
        this.facturacionService = facturacionService;
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

    /**
     * Vista de un único dominio en detalle
     */
    @GetMapping("/dominios/{id}")
    public String detalleDominio(
            @PathVariable int id,
            Model model) {

        Dominio dominio =
                gestionDominioService.obtenerDominio(id);

        List<HistorialDominio> historial =
                gestionDominioService.obtenerHistorial(id);

        Optional<Facturacion> facturacion =
                gestionDominioService.obtenerFacturacion(id);

        model.addAttribute("dominio", dominio);
        model.addAttribute("historial", historial);
        model.addAttribute(
                "facturacion",
                facturacion.orElse(null)
        );

        return "gestion/dominio-detalle";
    }

    @PostMapping("dominios/{id}/facturar")
    public String marcarComoFacturado(@PathVariable int id){

        facturacionService.marcarComoFacturado(id);

        return "redirect:/gestion/dominios/" + id;
    }
}