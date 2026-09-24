package com.dominiossolunet.service;

import com.dominiossolunet.dto.ErrorEnvioEmail;
import com.dominiossolunet.dto.ResultadoEnvioEmail;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    private EmailService emailService;

    @BeforeEach
    void setUp() {

        emailService = new EmailService(
                mailSender,
                templateEngine
        );

        /*
         * @Value no se ejecuta en un test unitario con Mockito,
         * por lo que inyectamos manualmente estos valores.
         */
        ReflectionTestUtils.setField(
                emailService,
                "remitente",
                "noreply@solunet.es"
        );

        ReflectionTestUtils.setField(
                emailService,
                "emailAdmin",
                "admin@solunet.es"
        );
    }


    // =========================================================
    // enviarAvisoRenovacion()
    // =========================================================

    @Test
    void enviarAvisoRenovacion_envioCorrecto_devuelveResultadoExitoso() throws Exception {

        // Arrange

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@test.com");

        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");

        List<Dominio> dominios = List.of(dominio);

        String urlRenovacion =
                "http://localhost/renovacion/token123";

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("email/aviso-renovacion"),
                any(Context.class)
        )).thenReturn("<html>Contenido</html>");


        // Act

        ResultadoEnvioEmail resultado =
                emailService.enviarAvisoRenovacion(
                        cliente,
                        dominios,
                        urlRenovacion
                );


        // Assert

        assertTrue(resultado.isEnviado());

        assertNull(resultado.getMensajeError());

        verify(mailSender).createMimeMessage();

        verify(templateEngine).process(
                eq("email/aviso-renovacion"),
                any(Context.class)
        );

        verify(mailSender).send(mimeMessage);
    }


    @Test
    void enviarAvisoRenovacion_errorAlEnviar_devuelveResultadoFallido()
            throws Exception {

        // Arrange

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@test.com");

        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");

        List<Dominio> dominios = List.of(dominio);

        String urlRenovacion =
                "http://localhost/renovacion/token123";

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("email/aviso-renovacion"),
                any(Context.class)
        )).thenReturn("<html>Contenido</html>");

        MailSendException excepcion =
                new MailSendException("Error de prueba");

        doThrow(excepcion).when(mailSender).send(mimeMessage);


        // Act

        ResultadoEnvioEmail resultado =
                emailService.enviarAvisoRenovacion(
                        cliente,
                        dominios,
                        urlRenovacion
                );


        // Assert

        assertFalse(resultado.isEnviado());

        assertEquals(
                "Error de prueba",
                resultado.getMensajeError()
        );

        verify(mailSender).createMimeMessage();

        verify(templateEngine).process(
                eq("email/aviso-renovacion"),
                any(Context.class)
        );

        verify(mailSender).send(mimeMessage);
    }


    @Test
    void enviarAvisoRenovacion_pasaDatosCorrectosAlTemplate() throws Exception {

        // Arrange

        Cliente cliente = new Cliente();
        cliente.setEmail("ana@test.com");

        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");

        List<Dominio> dominios = List.of(dominio);

        String urlRenovacion =
                "http://localhost/renovacion/token123";

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("email/aviso-renovacion"),
                any(Context.class)
        )).thenReturn("<html>Contenido</html>");


        // Act

        emailService.enviarAvisoRenovacion(
                cliente,
                dominios,
                urlRenovacion
        );


        // Assert

        ArgumentCaptor<Context> contextCaptor =
                ArgumentCaptor.forClass(Context.class);

        verify(templateEngine).process(
                eq("email/aviso-renovacion"),
                contextCaptor.capture()
        );

        Context context = contextCaptor.getValue();

        assertEquals(
                dominios,
                context.getVariable("listaDominios")
        );

        assertEquals(
                urlRenovacion,
                context.getVariable("urlRenovacion")
        );
    }


    // =========================================================
    // enviarInformeRenovacion()
    // =========================================================

    @Test
    void enviarInformeRenovacion_envioCorrecto_devuelveResultadoExitoso()
            throws Exception {

        // Arrange

        List<ErrorEnvioEmail> errores = List.of();

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("email/informe-renovacion-admin"),
                any(Context.class)
        )).thenReturn("<html>Informe</html>");


        // Act

        ResultadoEnvioEmail resultado =
                emailService.enviarInformeRenovacion(errores);


        // Assert

        assertTrue(resultado.isEnviado());

        assertNull(resultado.getMensajeError());

        verify(mailSender).createMimeMessage();

        verify(templateEngine).process(
                eq("email/informe-renovacion-admin"),
                any(Context.class)
        );

        verify(mailSender).send(mimeMessage);
    }


    @Test
    void enviarInformeRenovacion_errorAlEnviar_devuelveResultadoFallido()
            throws Exception {

        // Arrange

        List<ErrorEnvioEmail> errores = List.of();

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("email/informe-renovacion-admin"),
                any(Context.class)
        )).thenReturn("<html>Informe</html>");

        MailSendException excepcion =
                new MailSendException("Error enviando informe");

        doThrow(excepcion).when(mailSender).send(mimeMessage);


        // Act

        ResultadoEnvioEmail resultado =
                emailService.enviarInformeRenovacion(errores);


        // Assert

        assertFalse(resultado.isEnviado());

        assertEquals(
                "Error enviando informe",
                resultado.getMensajeError()
        );

        verify(mailSender).createMimeMessage();

        verify(templateEngine).process(
                eq("email/informe-renovacion-admin"),
                any(Context.class)
        );

        verify(mailSender).send(mimeMessage);
    }


    @Test
    void enviarInformeRenovacion_pasaErroresCorrectamenteAlTemplate()
            throws Exception {

        // Arrange

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@test.com");

        Dominio dominio = new Dominio();
        dominio.setNombreDominio("ana.com");

        ErrorEnvioEmail error =
                new ErrorEnvioEmail(
                        cliente,
                        List.of(dominio),
                        "Error de conexión"
                );

        List<ErrorEnvioEmail> errores =
                List.of(error);

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("email/informe-renovacion-admin"),
                any(Context.class)
        )).thenReturn("<html>Informe</html>");


        // Act

        emailService.enviarInformeRenovacion(errores);


        // Assert

        ArgumentCaptor<Context> contextCaptor =
                ArgumentCaptor.forClass(Context.class);

        verify(templateEngine).process(
                eq("email/informe-renovacion-admin"),
                contextCaptor.capture()
        );

        Context context = contextCaptor.getValue();

        assertEquals(
                errores,
                context.getVariable("erroresEnvio")
        );
    }
}
