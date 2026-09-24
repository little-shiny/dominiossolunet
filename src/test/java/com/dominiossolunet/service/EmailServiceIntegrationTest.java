package com.dominiossolunet.service;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.dto.ErrorEnvioEmail;
import com.dominiossolunet.dto.ResultadoEnvioEmail;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(EmailServiceIntegrationTest.MailTestConfiguration.class)
class EmailServiceIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail =
            new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired
    private EmailService emailService;

    @BeforeEach
    void limpiarCorreos() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {

        /*
         * Propiedades necesarias para que Spring pueda crear
         * todos los beans del contexto.
         */
        registry.add(
                "app.url.renovacion",
                () -> "http://localhost/renovacion"
        );

        registry.add(
                "spring.mail.username",
                () -> "test@solunet.es"
        );

        registry.add(
                "app.email.admin",
                () -> "admin@solunet.es"
        );
    }

    /**
     * Configuración de correo exclusiva para los tests.
     *
     * Se conecta al servidor SMTP de GreenMail
     * sin autenticación ni STARTTLS.
     */
    @TestConfiguration
    static class MailTestConfiguration {

        @Bean
        @Primary
        JavaMailSenderImpl testMailSender() {

            JavaMailSenderImpl mailSender =
                    new JavaMailSenderImpl();

            mailSender.setHost("localhost");
            mailSender.setPort(ServerSetupTest.SMTP.getPort());
            mailSender.setUsername("test@solunet.es");

            Properties properties =
                    mailSender.getJavaMailProperties();

            properties.setProperty(
                    "mail.smtp.auth",
                    "false"
            );

            properties.setProperty(
                    "mail.smtp.starttls.enable",
                    "false"
            );

            properties.setProperty(
                    "mail.smtp.starttls.required",
                    "false"
            );

            return mailSender;
        }
    }

    // =========================================================
    // enviarAvisoRenovacion
    // =========================================================

    @Test
    void enviarAvisoRenovacion_debeEnviarCorreoCorrectamente()
            throws Exception {

        // -----------------------------------------------------
        // Arrange
        // -----------------------------------------------------

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@cliente.com");

        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");

        List<Dominio> dominios = List.of(dominio);

        String urlRenovacion =
                "http://localhost/renovacion/token123";

        // -----------------------------------------------------
        // Act
        // -----------------------------------------------------

        ResultadoEnvioEmail resultado =
                emailService.enviarAvisoRenovacion(
                        cliente,
                        dominios,
                        urlRenovacion
                );

        // -----------------------------------------------------
        // Assert - resultado del servicio
        // -----------------------------------------------------

        assertThat(resultado)
                .isNotNull();

        assertThat(resultado.isEnviado())
                .isTrue();

        assertThat(resultado.getMensajeError())
                .isNull();

        // -----------------------------------------------------
        // Assert - GreenMail
        // -----------------------------------------------------

        MimeMessage[] mensajes =
                greenMail.getReceivedMessages();

        assertThat(mensajes)
                .hasSize(1);

        MimeMessage mensaje = mensajes[0];

        // -----------------------------------------------------
        // Destinatario
        // -----------------------------------------------------

        assertThat(mensaje.getAllRecipients())
                .isNotNull();

        assertThat(mensaje.getAllRecipients()[0].toString())
                .isEqualTo("ana@cliente.com");

        // -----------------------------------------------------
        // Remitente
        // -----------------------------------------------------

        assertThat(mensaje.getFrom())
                .isNotNull();

        assertThat(mensaje.getFrom()[0].toString())
                .contains("test@solunet.es");

        // -----------------------------------------------------
        // Asunto
        // -----------------------------------------------------

        assertThat(mensaje.getSubject())
                .isEqualTo(
                        "Dominios próximos a expirar - Solunet"
                );

        // -----------------------------------------------------
        // Contenido
        // -----------------------------------------------------

        String contenido =
                obtenerContenidoHtml(mensaje);

        assertThat(contenido)
                .isNotBlank();

        assertThat(contenido)
                .contains("ana.com");

        assertThat(contenido)
                .contains(urlRenovacion);

        // -----------------------------------------------------
        // Imagen inline
        // -----------------------------------------------------

        assertThat(contenido)
                .contains("logosolunet");
    }

    // =========================================================
    // enviarInformeRenovacion
    // =========================================================

    @Test
    void enviarInformeRenovacion_debeEnviarInformeAlAdministrador()
            throws Exception {

        // -----------------------------------------------------
        // Arrange
        // -----------------------------------------------------

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@cliente.com");

        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");

        List<Dominio> dominios = List.of(dominio);

        ErrorEnvioEmail error =
                new ErrorEnvioEmail(
                        cliente,
                        dominios,
                        "Error enviando correo"
                );

        List<ErrorEnvioEmail> errores =
                List.of(error);

        // -----------------------------------------------------
        // Act
        // -----------------------------------------------------

        ResultadoEnvioEmail resultado =
                emailService.enviarInformeRenovacion(errores);

        // -----------------------------------------------------
        // Assert - resultado del servicio
        // -----------------------------------------------------

        assertThat(resultado)
                .isNotNull();

        assertThat(resultado.isEnviado())
                .isTrue();

        assertThat(resultado.getMensajeError())
                .isNull();

        // -----------------------------------------------------
        // Assert - GreenMail
        // -----------------------------------------------------

        MimeMessage[] mensajes =
                greenMail.getReceivedMessages();

        assertThat(mensajes)
                .hasSize(1);

        MimeMessage mensaje = mensajes[0];

        // -----------------------------------------------------
        // Destinatario
        // -----------------------------------------------------

        assertThat(mensaje.getAllRecipients())
                .isNotNull();

        assertThat(mensaje.getAllRecipients()[0].toString())
                .isEqualTo("admin@solunet.es");

        // -----------------------------------------------------
        // Remitente
        // -----------------------------------------------------

        assertThat(mensaje.getFrom())
                .isNotNull();

        assertThat(mensaje.getFrom()[0].toString())
                .contains("test@solunet.es");

        // -----------------------------------------------------
        // Asunto
        // -----------------------------------------------------

        assertThat(mensaje.getSubject())
                .startsWith(
                        "Informe de avisos de renovación dominios - "
                );

        // -----------------------------------------------------
        // Contenido HTML
        // -----------------------------------------------------

        // -----------------------------------------------------
// Contenido HTML
// -----------------------------------------------------

        String contenido =
                obtenerContenidoHtml(mensaje);

        assertThat(contenido)
                .isNotBlank();

        assertThat(contenido)
                .contains("Ana");

        assertThat(contenido)
                .contains("ana@cliente.com");

        assertThat(contenido)
                .contains("ana.com");

        assertThat(contenido)
                .contains("Error enviando correo");

    }

    // =========================================================
    // Utilidades
    // =========================================================

    /**
     * Obtiene el contenido HTML de un MimeMessage.
     *
     * El correo generado por MimeMessageHelper es multipart
     * porque contiene HTML e imagen inline.
     */
    private String obtenerContenidoHtml(
            MimeMessage mensaje) throws Exception {

        Object contenido = mensaje.getContent();

        if (contenido instanceof MimeMultipart multipart) {
            return buscarHtml(multipart);
        }

        return contenido.toString();
    }

    /**
     * Recorre recursivamente las partes MIME hasta encontrar
     * la parte text/html.
     */
    private String buscarHtml(
            MimeMultipart multipart) throws Exception {

        for (int i = 0; i < multipart.getCount(); i++) {

            BodyPart parte = multipart.getBodyPart(i);

            Object contenido = parte.getContent();

            if (contenido instanceof MimeMultipart nestedMultipart) {

                String html = buscarHtml(nestedMultipart);

                if (html != null) {
                    return html;
                }

            } else if (
                    parte.isMimeType("text/html")) {

                return contenido.toString();
            }
        }

        return null;
    }
}