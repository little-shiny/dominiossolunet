package com.dominiossolunet.dto;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;

public class DominioGestionDto {

    private final Dominio dominio;
    private final Facturacion facturacion;
    private final boolean puedeRenovar;
    private final boolean puedeFacturar;

    public DominioGestionDto(
            Dominio dominio,
            Facturacion facturacion,
            boolean puedeRenovar,
            boolean puedeFacturar
    ) {
        this.dominio = dominio;
        this.facturacion = facturacion;
        this.puedeRenovar = puedeRenovar;
        this.puedeFacturar = puedeFacturar;
    }

    public Dominio getDominio() {
        return dominio;
    }

    public Facturacion getFacturacion() {
        return facturacion;
    }

    public boolean isPuedeRenovar() {
        return puedeRenovar;
    }

    public boolean isPuedeFacturar() {
        return puedeFacturar;
    }
}