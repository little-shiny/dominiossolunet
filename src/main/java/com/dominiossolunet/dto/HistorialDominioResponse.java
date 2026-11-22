package com.dominiossolunet.dto;

import com.dominiossolunet.model.enums.TipoEventoDominio;

import java.time.LocalDateTime;

public record HistorialDominioResponse(
        int id,
        TipoEventoDominio tipoEvento,
        LocalDateTime fecha,
        String detalle
) {
}