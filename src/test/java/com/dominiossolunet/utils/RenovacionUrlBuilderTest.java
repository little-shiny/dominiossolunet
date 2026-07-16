package com.dominiossolunet.utils;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@TestPropertySource(properties = "app.url.dominio=facturacion.solunet.es")
public class RenovacionUrlBuilderTest {
    @Autowired
    private RenovacionUrlBuilder renovacionUrlBuilder;

    @Test
    void verificarConstruirUrlConfirmacion() {

        String urlCorrecta = "facturacion.solunet.es/renovacion/?t=abcd1234";
        String tokenTest = "abcd1234";

        assertThat(renovacionUrlBuilder.construirUrlConfirmacion(tokenTest).equals(urlCorrecta));
    }
}