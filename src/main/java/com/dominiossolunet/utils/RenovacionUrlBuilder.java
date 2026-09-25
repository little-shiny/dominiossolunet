package com.dominiossolunet.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RenovacionUrlBuilder {
    final String urlBase;
    final String urlRenovacion;

    public RenovacionUrlBuilder(@Value("${app.url.dominio}") String urlBase,
                                @Value("${app.url.renovacion}") String urlRenovacion) {
        this.urlBase = urlBase;
        this.urlRenovacion = urlRenovacion;
    }

    public String construirUrlConfirmacion(String token) {
        return urlBase + urlRenovacion + "?token=" + token;
    }
}
