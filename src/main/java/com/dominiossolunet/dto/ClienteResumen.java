package com.dominiossolunet.dto;

import com.dominiossolunet.model.Cliente;
import lombok.Getter;

@Getter
public class ClienteResumen {

    private final Cliente cliente;
    private final long totalDominios;
    private final long pendientesRenovacion;
    private final long renovados;
    private final long rechazados;
    private final long pendientesFacturacion;

    public ClienteResumen(
            Cliente cliente,
            long totalDominios,
            long pendientesRenovacion,
            long renovados,
            long rechazados,
            long pendientesFacturacion) {

        this.cliente = cliente;
        this.totalDominios = totalDominios;
        this.pendientesRenovacion = pendientesRenovacion;
        this.renovados = renovados;
        this.rechazados = rechazados;
        this.pendientesFacturacion = pendientesFacturacion;
    }

}