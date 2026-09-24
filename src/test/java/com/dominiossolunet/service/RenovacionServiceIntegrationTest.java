package com.dominiossolunet.service;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.TokenDominio;
import com.dominiossolunet.model.enums.Estado;
import com.dominiossolunet.model.enums.EstadoAvisoRenovacion;
import com.dominiossolunet.model.enums.Registrador;
import com.dominiossolunet.repository.ClienteRepository;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.TokenClienteRepository;
import com.dominiossolunet.repository.TokenDominioRepository;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RenovacionServiceIntegrationTest.MailTestConfiguration.class)
class RenovacionServiceIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail =
            new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired
    private RenovacionService renovacionService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DominioRepository dominioRepository;

    @Autowired
    private TokenClienteRepository tokenClienteRepository;

    @Autowired
    private TokenDominioRepository tokenDominioRepository;

    @BeforeEach
    void limpiarDatos() throws FolderException {
        tokenDominioRepository.deleteAll();
        tokenClienteRepository.deleteAll();
        dominioRepository.deleteAll();
        clienteRepository.deleteAll();

        greenMail.purgeEmailFromAllMailboxes();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add(
                "renovacion.umbrales",
                () -> "30,15,5,1"
        );

        registry.add(
                "app.url.renovacion",
                () -> "http://localhost/renovacion/"
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

    @TestConfiguration
    static class MailTestConfiguration {

        @Bean
        @Primary
        JavaMailSenderImpl testMailSender() {

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

            mailSender.setHost("localhost");
            mailSender.setPort(ServerSetupTest.SMTP.getPort());
            mailSender.setUsername("test@solunet.es");

            Properties properties = mailSender.getJavaMailProperties();

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

    @Test
    void procesarAvisos_flujoCompleto_debeCrearTokenActualizarDominioYEnviarCorreos()
            throws Exception {

        // ---------------------------------------------------------
        // ARRANGE
        // ---------------------------------------------------------

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@cliente.com");

        Cliente clienteGuardado =
                clienteRepository.saveAndFlush(cliente);

        Dominio dominio = new Dominio();

        dominio.setCliente(clienteGuardado);
        dominio.setNombreDominio("ana.com");
        dominio.setEstado(Estado.ACTIVO);
        dominio.setFechaExpiracion(
                LocalDate.now().plusDays(20)
        );
        dominio.setRegistrador(Registrador.DOMITECA);

        Dominio dominioGuardado =
                dominioRepository.saveAndFlush(dominio);

        // ---------------------------------------------------------
        // ACT
        // ---------------------------------------------------------

        renovacionService.procesarAvisos();

        // ---------------------------------------------------------
        // ASSERT - DOMINIO
        // ---------------------------------------------------------

        Dominio dominioActualizado =
                dominioRepository
                        .findById(dominioGuardado.getId())
                        .orElseThrow();

        assertThat(dominioActualizado.getEstado())
                .isEqualTo(Estado.AVISO_ENVIADO);

        assertThat(dominioActualizado.getUltimoUmbralAvisado())
                .isEqualTo(30);

        assertThat(dominioActualizado.getUltimoAviso())
                .isEqualTo(LocalDate.now());

        // ---------------------------------------------------------
        // ASSERT - TOKEN CLIENTE
        // ---------------------------------------------------------

        List<TokenCliente> tokens =
                tokenClienteRepository.findAll();

        assertThat(tokens)
                .hasSize(1);

        TokenCliente tokenCliente =
                tokens.get(0);

        assertThat(tokenCliente.getToken())
                .isNotBlank();

        assertThat(tokenCliente.isUsado())
                .isFalse();

        assertThat(tokenCliente.getFechaCreacion())
                .isNotNull();

        assertThat(tokenCliente.getFechaExpiracion())
                .isNotNull();

        // ---------------------------------------------------------
        // ASSERT - TOKEN DOMINIO
        // ---------------------------------------------------------

        TokenDominio tokenDominio =
                tokenDominioRepository
                        .findByDominio(dominioGuardado)
                        .orElseThrow();

        assertThat(tokenDominio.getEstadoAvisoRenovacion())
                .isEqualTo(
                        EstadoAvisoRenovacion.PENDIENTE
                );

        // ---------------------------------------------------------
        // ASSERT - CORREOS
        // ---------------------------------------------------------

        Message[] mensajes =
                greenMail.getReceivedMessages();

        /*
         * Se esperan dos correos:
         *
         * 1. Aviso al cliente
         * 2. Informe al administrador
         */
        assertThat(mensajes)
                .hasSize(2);

        // ---------------------------------------------------------
        // ASSERT - CORREO CLIENTE
        // ---------------------------------------------------------

        Message emailCliente =
                buscarMensajePorDestinatario(
                        mensajes,
                        "ana@cliente.com"
                );

        assertThat(emailCliente)
                .isNotNull();

        assertThat(emailCliente.getSubject())
                .contains(
                        "Dominios próximos a expirar"
                );

        String contenidoCliente =
                obtenerContenidoTexto(emailCliente
                );

        assertThat(contenidoCliente)
                .contains("ana.com");

        assertThat(contenidoCliente)
                .contains(
                        "http://localhost/renovacion/"
                );

        // ---------------------------------------------------------
        // ASSERT - CORREO ADMINISTRADOR
        // ---------------------------------------------------------

        Message emailAdministrador =
                buscarMensajePorDestinatario(
                        mensajes,
                        "admin@solunet.es"
                );

        assertThat(emailAdministrador)
                .isNotNull();

        assertThat(emailAdministrador.getSubject())
                .contains(
                        "Informe de avisos de renovación dominios"
                );

        String contenidoAdministrador =
                obtenerContenidoTexto(emailAdministrador);

        assertThat(contenidoAdministrador)
                .isNotBlank();
    }

    /**
     * Busca dentro de los mensajes recibidos aquel cuyo destinatario
     * coincida con el correo indicado.
     */
    private Message buscarMensajePorDestinatario(
            Message[] mensajes,
            String email
    ) throws Exception {

        for (Message mensaje : mensajes) {

            if (mensaje.getAllRecipients() == null) {
                continue;
            }

            for (var destinatario :
                    mensaje.getAllRecipients()) {

                if (destinatario
                        .toString()
                        .equalsIgnoreCase(email)) {

                    return mensaje;
                }
            }
        }

        return null;
    }

    private String obtenerContenidoTexto(Message mensaje) throws Exception {

        Object contenido = mensaje.getContent();

        if (contenido instanceof String) {
            return contenido.toString();
        }

        if (contenido instanceof jakarta.mail.Multipart multipart) {

            for (int i = 0; i < multipart.getCount(); i++) {

                jakarta.mail.BodyPart parte =
                        multipart.getBodyPart(i);

                Object contenidoParte =
                        parte.getContent();

                if (contenidoParte instanceof String texto) {
                    return texto;
                }

                if (contenidoParte instanceof jakarta.mail.Multipart subMultipart) {
                    for (int j = 0; j < subMultipart.getCount(); j++) {

                        Object contenidoSubParte =
                                subMultipart
                                        .getBodyPart(j)
                                        .getContent();

                        if (contenidoSubParte instanceof String texto) {
                            return texto;
                        }
                    }
                }
            }
        }

        return "";
    }
}
