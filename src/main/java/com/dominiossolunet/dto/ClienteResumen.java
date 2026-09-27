package com.dominiossolunet.dto;

import com.dominiossolunet.model.Cliente;

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

    public Cliente getCliente() {
        return cliente;
    }

    public long getTotalDominios() {
        return totalDominios;
    }

    public long getPendientesRenovacion() {
        return pendientesRenovacion;
    }

    public long getRenovados() {
        return renovados;
    }

    public long getRechazados() {
        return rechazados;
    }

    public long getPendientesFacturacion() {
        return pendientesFacturacion;
    }
}