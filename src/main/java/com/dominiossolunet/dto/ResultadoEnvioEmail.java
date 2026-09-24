package com.dominiossolunet.dto;

import lombok.Getter;

@Getter
public class ResultadoEnvioEmail {
    private final boolean enviado;
    private final String mensajeError;


    public ResultadoEnvioEmail(boolean enviado, String mensajeError) {
        this.enviado = enviado;
        this.mensajeError = mensajeError;
    }
}
