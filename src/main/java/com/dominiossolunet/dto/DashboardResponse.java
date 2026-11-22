package com.dominiossolunet.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalDominios,
        long dominiosActivos,
        long pendientesRenovacion,
        long renovacionesRealizadas,
        long renovacionesRechazadas,
        long expirados,
        long expiranEn30Dias,
        List<DominioProximaExpiracionResponse> proximasExpiraciones,
        Map<String, Long> dominiosPorRegistrador
) {
}