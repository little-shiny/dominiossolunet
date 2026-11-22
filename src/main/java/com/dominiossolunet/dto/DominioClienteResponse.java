package com.dominiossolunet.dto;

import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.model.enums.Registrador;

import java.time.LocalDate;

public record DominioClienteResponse(
        int id,
        String nombreDominio,
        LocalDate fechaExpiracion,
        Estado estado,
        EstadoRenovacion estadoRenovacion,
        Registrador registrador,
        LocalDate ultimoAviso
) {
}