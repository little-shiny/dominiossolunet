package com.dominiossolunet.dto;

import com.dominiossolunet.model.enums.ResultadoValidacion;

import java.util.List;

public record RenovacionTokenResponse(
        ResultadoValidacion resultado,
        String clienteNombre,
        List<RenovacionDominioResponse> dominios
) {
}