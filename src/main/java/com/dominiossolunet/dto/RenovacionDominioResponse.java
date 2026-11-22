package com.dominiossolunet.dto;

import java.time.LocalDate;

public record RenovacionDominioResponse(
        int id,
        String nombreDominio,
        LocalDate fechaExpiracion
) {
}