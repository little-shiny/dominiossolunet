package com.dominiossolunet.controller;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/gestion/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listarClientes(Model model) {

        model.addAttribute(
                "clientes",
                clienteService.obtenerResumenClientes());

        return "gestion/clientes";
    }

    @GetMapping("/{id}")
    public String detalleCliente(
            @PathVariable int id,
            Model model) {

        Cliente cliente =
                clienteService.obtenerCliente(id);

        List<Dominio> dominios =
                clienteService.obtenerDominiosCliente(id);

        model.addAttribute("cliente", cliente);
        model.addAttribute("dominios", dominios);

        model.addAttribute(
                "totalDominios",
                clienteService.contarDominios(dominios));

        model.addAttribute(
                "pendientesRenovacion",
                clienteService.contarPendientesRenovacion(dominios));

        model.addAttribute(
                "renovados",
                clienteService.contarRenovados(dominios));

        model.addAttribute(
                "rechazados",
                clienteService.contarRechazados(dominios));

        model.addAttribute(
                "pendientesFacturacion",
                clienteService.contarPendientesFacturacion(dominios));

        return "gestion/cliente-detalle";
    }
}