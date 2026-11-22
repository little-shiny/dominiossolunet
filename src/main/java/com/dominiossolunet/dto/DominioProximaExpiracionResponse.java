package com.dominiossolunet.dto;

import java.time.LocalDate;

public record DominioProximaExpiracionResponse(
        int id,
        String nombreDominio,
        String clienteNombre,
        LocalDate fechaExpiracion,
        String estadoRenovacion
) {
}