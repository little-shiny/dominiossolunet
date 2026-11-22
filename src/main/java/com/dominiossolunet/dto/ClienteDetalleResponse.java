package com.dominiossolunet.dto;

import java.util.List;

public record ClienteDetalleResponse(
        int id,
        String nombre,
        String email,
        long totalDominios,
        long pendientesRenovacion,
        long renovados,
        long rechazados,
        long pendientesFacturacion,
        List<DominioClienteResponse> dominios
) {
}