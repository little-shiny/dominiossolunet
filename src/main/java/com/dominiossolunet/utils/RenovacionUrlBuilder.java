package com.dominiossolunet.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RenovacionUrlBuilder {
    @Value("${app.url.dominio}")
    final String urlBase;

    // Url que añade al dominio
    final String urlRenovacion = "/renovacion/?t=";

    public RenovacionUrlBuilder(String urlBase){
        this.urlBase = urlBase;
    }

    public String contruirUrlConfirmacion(String token){
        return urlBase + urlRenovacion + token;
    }
}
