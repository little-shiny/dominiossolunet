package com.dominiossolunet.dto;

import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;

import java.time.LocalDate;
import java.util.List;

public record DominioDetalleResponse(
        int id,
        String nombreDominio,
        int clienteId,
        String clienteNombre,
        String clienteEmail,
        LocalDate fechaExpiracion,
        Estado estado,
        EstadoRenovacion estadoRenovacion,
        Registrador registrador,
        LocalDate ultimoAviso,
        FacturacionResponse facturacion,
        boolean puedeRenovar,
        List<HistorialDominioResponse> historial
) {
}