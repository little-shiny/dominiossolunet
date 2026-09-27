package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final GestionDominioService gestionDominioService;

    public DashboardService(GestionDominioService gestionDominioService) {
        this.gestionDominioService = gestionDominioService;
    }

    public List<Dominio> obtenerDominios() {
        return gestionDominioService.obtenerTodosLosDominios();
    }

    public long totalDominios(List<Dominio> dominios) {
        return dominios.size();
    }

    public long dominiosActivos(List<Dominio> dominios) {
        return dominios.stream()
                .filter(d -> d.getEstado() == Estado.ACTIVO)
                .count();
    }

    public long pendientesRenovacion(List<Dominio> dominios) {
        return dominios.stream()
                .filter(d -> d.getEstadoRenovacion() == EstadoRenovacion.PENDIENTE_RENOVACION)
                .count();
    }

    public long renovacionesRealizadas(List<Dominio> dominios) {
        return dominios.stream()
                .filter(d -> d.getEstadoRenovacion() == EstadoRenovacion.RENOVADO)
                .count();
    }

    public long renovacionesRechazadas(List<Dominio> dominios) {
        return dominios.stream()
                .filter(d -> d.getEstadoRenovacion() == EstadoRenovacion.RECHAZADO)
                .count();
    }

    public long expirados(List<Dominio> dominios) {
        LocalDate hoy = LocalDate.now();

        return dominios.stream()
                .filter(d -> d.getFechaExpiracion() != null)
                .filter(d -> d.getFechaExpiracion().isBefore(hoy))
                .count();
    }

    public long expiranEn30Dias(List<Dominio> dominios) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(30);

        return dominios.stream()
                .filter(d -> d.getFechaExpiracion() != null)
                .filter(d -> !d.getFechaExpiracion().isBefore(hoy))
                .filter(d -> !d.getFechaExpiracion().isAfter(limite))
                .count();
    }

    public List<Dominio> obtenerProximasExpiraciones(List<Dominio> dominios) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(30);

        return dominios.stream()
                .filter(d -> d.getFechaExpiracion() != null)
                .filter(d -> !d.getFechaExpiracion().isBefore(hoy))
                .filter(d -> !d.getFechaExpiracion().isAfter(limite))
                .sorted(Comparator.comparing(Dominio::getFechaExpiracion))
                .limit(10)
                .collect(Collectors.toList());
    }

    public Map<Registrador, Long> dominiosPorRegistrador(List<Dominio> dominios) {
        return dominios.stream()
                .filter(d -> d.getRegistrador() != null)
                .collect(Collectors.groupingBy(
                        Dominio::getRegistrador,
                        Collectors.counting()
                ));
    }
}
