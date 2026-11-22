package com.dominiossolunet.dto;

import com.dominiossolunet.model.enums.EstadoFacturacion;

import java.time.LocalDate;

public record FacturacionResponse(
        EstadoFacturacion estadoFacturacion,
        LocalDate fechaUltimaFactura,
        String nota
) {
}