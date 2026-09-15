package com.dominiossolunet.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RenovacionUrlBuilderTest {

    @Test
    void construirUrlConfirmacion_construyeLaUrlCorrectamente() {

        // Arrange
        String urlBase = "https://facturacion.solunet.es";
        String urlRenovacion = "/renovacion/";
        String token = "abc123";

        RenovacionUrlBuilder renovacionUrlBuilder =
                new RenovacionUrlBuilder(urlBase, urlRenovacion);

        String urlEsperada =
                "https://facturacion.solunet.es/renovacion/abc123";

        // Act
        String resultado =
                renovacionUrlBuilder.construirUrlConfirmacion(token);

        // Assert
        assertThat(resultado)
                .isEqualTo(urlEsperada);
    }


    @Test
    void construirUrlConfirmacion_conTokenDiferente_utilizaElTokenRecibido() {

        // Arrange
        RenovacionUrlBuilder renovacionUrlBuilder =
                new RenovacionUrlBuilder(
                        "https://facturacion.solunet.es",
                        "/renovacion/"
                );

        // Act
        String resultado =
                renovacionUrlBuilder.construirUrlConfirmacion("xyz789");

        // Assert
        assertThat(resultado)
                .isEqualTo(
                        "https://facturacion.solunet.es/renovacion/xyz789"
                );
    }


    @Test
    void construirUrlConfirmacion_conTokenVacio_devuelveLaUrlSinToken() {

        // Arrange
        RenovacionUrlBuilder renovacionUrlBuilder =
                new RenovacionUrlBuilder(
                        "https://facturacion.solunet.es",
                        "/renovacion/"
                );

        // Act
        String resultado =
                renovacionUrlBuilder.construirUrlConfirmacion("");

        // Assert
        assertThat(resultado)
                .isEqualTo(
                        "https://facturacion.solunet.es/renovacion/"
                );
    }
}