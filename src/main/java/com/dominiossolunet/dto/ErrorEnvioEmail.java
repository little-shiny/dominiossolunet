package com.dominiossolunet.dto;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import lombok.Getter;
import java.util.List;

/**
 * Clase que representa un error al enviar un email en EmailService
 */
@Getter
public class ErrorEnvioEmail {
    private final Cliente cliente;
    private final List<Dominio> dominios;
    private final String mensajeError;

    public ErrorEnvioEmail(Cliente cliente, List<Dominio> dominios, String mensajeError){
        this.cliente = cliente;
        this.dominios = dominios;
        this.mensajeError = mensajeError;
    }
}
