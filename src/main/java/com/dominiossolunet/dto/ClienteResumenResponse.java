package com.dominiossolunet.dto;

public record ClienteResumenResponse(
        int id,
        String nombre,
        String email,
        long totalDominios,
        long pendientesRenovacion,
        long renovados,
        long rechazados,
        long pendientesFacturacion
) {
}