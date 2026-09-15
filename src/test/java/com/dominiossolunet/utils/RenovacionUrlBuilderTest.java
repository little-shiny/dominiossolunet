package com.dominiossolunet.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(
        properties = "app.url.dominio=facturacion.solunet.es"
)
class RenovacionUrlBuilderTest {

    @Autowired
    private RenovacionUrlBuilder renovacionUrlBuilder;

    @Test
    void construirUrlConfirmacion_debeGenerarUrlCorrecta() {

        // Arrange
        String tokenTest = "abcd1234";

        String urlEsperada =
                "facturacion.solunet.es/renovacion/?t=abcd1234";

        // Act
        String urlObtenida =
                renovacionUrlBuilder.construirUrlConfirmacion(tokenTest);

        // Assert
        assertThat(urlObtenida).isEqualTo(urlEsperada);
    }
}