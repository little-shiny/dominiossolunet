package com.dominiossolunet.controller;

import com.dominiossolunet.dto.DominioDetalleResponse;
import com.dominiossolunet.dto.DominioResumenResponse;
import com.dominiossolunet.dto.FacturacionResponse;
import com.dominiossolunet.dto.HistorialDominioResponse;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.service.FacturacionService;
import com.dominiossolunet.service.GestionDominioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/dominios")
public class DominioRestController {

    private final GestionDominioService gestionDominioService;
    private final FacturacionService facturacionService;

    public DominioRestController(GestionDominioService gestionDominioService, FacturacionService facturacionService) {
        this.gestionDominioService = gestionDominioService;
        this.facturacionService = facturacionService;
    }

    @GetMapping
    public List<DominioResumenResponse> listarDominios(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Estado estado,
            @RequestParam(required = false) EstadoRenovacion estadoRenovacion,
            @RequestParam(required = false) Registrador registrador,
            @RequestParam(required = false) String expiracion
    ) {
        LocalDate hoy = LocalDate.now();
        String texto = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);

        return gestionDominioService.obtenerTodosLosDominios().stream()
                .filter(d -> texto.isBlank()
                        || d.getNombreDominio().toLowerCase(Locale.ROOT).contains(texto)
                        || d.getCliente().getNombre().toLowerCase(Locale.ROOT).contains(texto)
                        || d.getCliente().getEmail().toLowerCase(Locale.ROOT).contains(texto))
                .filter(d -> clienteId == null || d.getCliente().getId() == clienteId)
                .filter(d -> estado == null || d.getEstado() == estado)
                .filter(d -> estadoRenovacion == null
                        || d.getEstadoRenovacion() == estadoRenovacion)
                .filter(d -> registrador == null || d.getRegistrador() == registrador)
                .filter(d -> cumpleFiltroExpiracion(d, expiracion, hoy))
                .map(this::convertirARespuesta)
                .toList();
    }

    @GetMapping("/{id}")
    public DominioDetalleResponse detalleDominio(@PathVariable int id) {
        Dominio dominio = gestionDominioService.obtenerDominio(id);

        FacturacionResponse facturacion = gestionDominioService
                .obtenerFacturacion(id)
                .map(f -> new FacturacionResponse(
                        f.getEstadoFacturacion(),
                        f.getFechaUltimaFactura(),
                        f.getNota()
                ))
                .orElse(null);

        List<HistorialDominioResponse> historial = gestionDominioService
                .obtenerHistorial(id)
                .stream()
                .map(evento -> new HistorialDominioResponse(
                        evento.getId(),
                        evento.getTipoEvento(),
                        evento.getFecha(),
                        evento.getDetalle()
                ))
                .toList();

        return new DominioDetalleResponse(
                dominio.getId(),
                dominio.getNombreDominio(),
                dominio.getCliente().getId(),
                dominio.getCliente().getNombre(),
                dominio.getCliente().getEmail(),
                dominio.getFechaExpiracion(),
                dominio.getEstado(),
                dominio.getEstadoRenovacion(),
                dominio.getRegistrador(),
                dominio.getUltimoAviso(),
                facturacion,
                gestionDominioService.puedeMarcarComoRenovado(id),
                historial
        );
    }

    @PostMapping("/{id}/renovar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoRenovado(@PathVariable int id) {
        gestionDominioService.marcarComoRenovado(id);
    }

    @PostMapping("/{id}/facturar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoFacturado(@PathVariable int id) {
        facturacionService.marcarComoFacturado(id);
    }
    private boolean cumpleFiltroExpiracion(
            Dominio dominio,
            String expiracion,
            LocalDate hoy
    ) {
        if (expiracion == null || expiracion.isBlank()) {
            return true;
        }

        LocalDate fecha = dominio.getFechaExpiracion();
        if (fecha == null) {
            return false;
        }

        return switch (expiracion) {
            case "7" -> !fecha.isBefore(hoy) && !fecha.isAfter(hoy.plusDays(7));
            case "30" -> !fecha.isBefore(hoy) && !fecha.isAfter(hoy.plusDays(30));
            case "expirados" -> fecha.isBefore(hoy);
            default -> true;
        };
    }

    private DominioResumenResponse convertirARespuesta(Dominio dominio) {
        return new DominioResumenResponse(
                dominio.getId(),
                dominio.getNombreDominio(),
                dominio.getCliente().getId(),
                dominio.getCliente().getNombre(),
                dominio.getCliente().getEmail(),
                dominio.getFechaExpiracion(),
                dominio.getEstado(),
                dominio.getEstadoRenovacion(),
                dominio.getRegistrador(),
                dominio.getUltimoAviso(),
                gestionDominioService.puedeMarcarComoRenovado(dominio.getId())
        );
    }
}