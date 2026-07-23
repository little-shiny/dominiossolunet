package com.dominiossolunet.dto;

import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.enums.ResultadoValidacionToken;

/**
 * Record creado para transferir datos desde la salida de la validación del token conteniendo el resultado y el
 * TokenCliente para poder ser utilizado a posteriori en una única transacción
 * @param resultado
 * @param token
 */
public record ResultadoValidacion(ResultadoValidacionToken resultado, TokenCliente token) {
}
