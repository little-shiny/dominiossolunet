package com.dominiossolunet.dto;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;

import java.util.List;

/**
 * Clase que representa un error al enviar un email en EmailService
 */
public record ErrorEnvioEmail(Cliente cliente, List<Dominio> dominios, String mensajeError) {
}
