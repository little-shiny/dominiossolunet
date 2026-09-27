package com.dominiossolunet.controller;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.HistorialDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.service.FacturacionService;
import com.dominiossolunet.service.GestionDominioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/gestion")
public class GestionDominioController {

    private final GestionDominioService gestionDominioService;
    private final FacturacionService facturacionService;

    public GestionDominioController(
            GestionDominioService gestionDominioService,
            FacturacionService facturacionService) {

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
    public String listarDominios(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Estado estado,
            @RequestParam(required = false) EstadoRenovacion estadoRenovacion,
            @RequestParam(required = false) Registrador registrador,
            @RequestParam(required = false) String expiracion,
            Model model) {

        /*
         * Obtenemos todos los dominios una sola vez.
         */
        List<Dominio> todosLosDominios =
                gestionDominioService.obtenerTodosLosDominios();

        /*
         * Empezamos con todos los dominios y vamos aplicando
         * los filtros seleccionados.
         */
        List<Dominio> dominios = todosLosDominios;

        /*
         * FILTRO DE BÚSQUEDA
         *
         * Busca por:
         * - nombre del dominio
         * - nombre del cliente
         * - email del cliente
         */
        if (busqueda != null && !busqueda.isBlank()) {

            String texto = busqueda.trim().toLowerCase();

            dominios = dominios.stream()
                    .filter(dominio ->
                            dominio.getNombreDominio()
                                    .toLowerCase()
                                    .contains(texto)
                                    ||
                                    dominio.getCliente()
                                            .getNombre()
                                            .toLowerCase()
                                            .contains(texto)
                                    ||
                                    dominio.getCliente()
                                            .getEmail()
                                            .toLowerCase()
                                            .contains(texto)
                    )
                    .collect(Collectors.toList());
        }

        /*
         * FILTRO POR CLIENTE
         */
        if (clienteId != null) {

            dominios = dominios.stream()
                    .filter(dominio ->
                            dominio.getCliente().getId() == clienteId)
                    .collect(Collectors.toList());
        }

        /*
         * FILTRO POR ESTADO
         */
        if (estado != null) {

            dominios = dominios.stream()
                    .filter(dominio ->
                            dominio.getEstado() == estado)
                    .collect(Collectors.toList());
        }

        /*
         * FILTRO POR ESTADO DE RENOVACIÓN
         */
        if (estadoRenovacion != null) {

            dominios = dominios.stream()
                    .filter(dominio ->
                            dominio.getEstadoRenovacion() == estadoRenovacion)
                    .collect(Collectors.toList());
        }

        /*
         * FILTRO POR REGISTRADOR
         */
        if (registrador != null) {

            dominios = dominios.stream()
                    .filter(dominio ->
                            dominio.getRegistrador() == registrador)
                    .collect(Collectors.toList());
        }

        /*
         * FILTRO POR FECHA DE EXPIRACIÓN
         *
         * 7          -> próximos 7 días
         * 30         -> próximos 30 días
         * expirados  -> fecha anterior a hoy
         */
        if (expiracion != null && !expiracion.isBlank()) {

            LocalDate hoy = LocalDate.now();

            switch (expiracion) {

                case "7":

                    dominios = dominios.stream()
                            .filter(dominio ->
                                    dominio.getFechaExpiracion() != null
                                            &&
                                            !dominio.getFechaExpiracion()
                                                    .isBefore(hoy)
                                            &&
                                            !dominio.getFechaExpiracion()
                                                    .isAfter(hoy.plusDays(7))
                            )
                            .collect(Collectors.toList());

                    break;

                case "30":

                    dominios = dominios.stream()
                            .filter(dominio ->
                                    dominio.getFechaExpiracion() != null
                                            &&
                                            !dominio.getFechaExpiracion()
                                                    .isBefore(hoy)
                                            &&
                                            !dominio.getFechaExpiracion()
                                                    .isAfter(hoy.plusDays(30))
                            )
                            .collect(Collectors.toList());

                    break;

                case "expirados":

                    dominios = dominios.stream()
                            .filter(dominio ->
                                    dominio.getFechaExpiracion() != null
                                            &&
                                            dominio.getFechaExpiracion()
                                                    .isBefore(hoy)
                            )
                            .collect(Collectors.toList());

                    break;

                default:
                    /*
                     * Si llega un valor desconocido no aplicamos
                     * ningún filtro de expiración.
                     */
                    break;
            }
        }

        /*
         * CLIENTES DISPONIBLES PARA EL SELECT
         *
         * Los obtenemos a partir de todos los dominios,
         * no de la lista ya filtrada.
         *
         * De esta forma el selector de clientes siempre
         * muestra todos los clientes disponibles.
         */
        List<Cliente> clientes = todosLosDominios.stream()
                .map(Dominio::getCliente)
                .distinct()
                .sorted((a, b) ->
                        a.getNombre()
                                .compareToIgnoreCase(b.getNombre()))
                .collect(Collectors.toList());

        /*
         * DATOS NECESARIOS PARA LOS SELECTS DEL HTML
         */
        model.addAttribute("clientes", clientes);

        model.addAttribute(
                "estados",
                Arrays.asList(Estado.values())
        );

        model.addAttribute(
                "estadosRenovacion",
                Arrays.asList(EstadoRenovacion.values())
        );

        model.addAttribute(
                "registradores",
                Arrays.asList(Registrador.values())
        );

        /*
         * RESULTADO DEL FILTRADO
         */
        model.addAttribute("dominios", dominios);

        /*
         * Conservamos los valores seleccionados para que
         * el formulario mantenga los filtros después de
         * pulsar "Filtrar".
         */
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("estado", estado);
        model.addAttribute("estadoRenovacion", estadoRenovacion);
        model.addAttribute("registrador", registrador);
        model.addAttribute("expiracion", expiracion);

        return "gestion/dominios";
    }

    /**
     * Vista de un único dominio en detalle.
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

    @PostMapping("/dominios/{id}/facturar")
    public String marcarComoFacturado(
            @PathVariable int id) {

        facturacionService.marcarComoFacturado(id);

        return "redirect:/gestion/dominios/" + id;
    }
}