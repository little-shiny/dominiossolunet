package com.dominiossolunet.service;

import com.dominiossolunet.dto.ErrorEnvioEmail;
import com.dominiossolunet.dto.ResultadoEnvioEmail;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${app.email.admin}")
    private String emailAdmin;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


//    --------------------------------------------------------------------
//    ENVÍOS DE AVISOS DE RENOVACIÓN
//    --------------------------------------------------------------------
    public ResultadoEnvioEmail enviarAvisoRenovacion(Cliente cliente, List<Dominio> dominiosRenovar, String urlRenovacion) {

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

            String html = construirAvisoRenovacionHtml(dominiosRenovar, urlRenovacion);

            helper.setTo(cliente.getEmail());
            helper.setFrom(remitente);
            helper.setSubject("Dominios próximos a expirar - Solunet");
            helper.setText(html, true);
            helper.addInline("logosolunet", new ClassPathResource("static/images/logosolunet.png"));
            mailSender.send(mensaje);
            logger.info("Email de renovación enviado correctamente a {}", cliente.getEmail());

            return new ResultadoEnvioEmail(true,null);

        } catch (MessagingException | MailException e) {
            logger.error("Error enviando el aviso al cliente {}", cliente.getEmail(),e);
            return new ResultadoEnvioEmail(false, e.getMessage());
        }
    }
//    --------------------------------------------------------------------
//    ENVÍO INFORME ADMINISTRADOR
//    --------------------------------------------------------------------
    public ResultadoEnvioEmail enviarInformeRenovacion(List<ErrorEnvioEmail> erroresEnvio){
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

            String html = construirInformeAdminHtml(erroresEnvio);

            helper.setTo(emailAdmin);
            helper.setFrom(remitente);
            helper.setSubject("Informe de avisos de renovación dominios - " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            helper.setText(html, true);
            helper.addInline("logosolunet", new ClassPathResource("static/images/logosolunet.png"));
            mailSender.send(mensaje);
            logger.info("Email de informe enviado correctamente a {}", emailAdmin);

            return new ResultadoEnvioEmail(true,null);

        } catch (MessagingException | MailException e) {
            logger.error("Error enviando el informe de renovación al administrador",e);
            return new ResultadoEnvioEmail(false, e.getMessage());
        }
    }

    private String construirAvisoRenovacionHtml(
            List<Dominio> dominios,
            String urlRenovacion
    ) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String elementos = dominios.stream()
                .map(d -> {
                    String fecha = d.getFechaExpiracion() == null
                            ? "sin fecha de expiración"
                            : d.getFechaExpiracion().format(formato);

                    return "<li>" + escaparHtml(d.getNombreDominio())
                            + " expirará el " + escaparHtml(fecha) + "</li>";
                })
                .collect(java.util.stream.Collectors.joining());

        return """
            <!doctype html>
            <html lang="es">
            <body>
              <img src="cid:logosolunet" alt="Solunet" width="180">
              <p>Buenos días,</p>
              <p>Le comunicamos que los siguientes dominios están próximos a expirar:</p>
              <ul>%s</ul>
              <p>Para solicitar la renovación, acceda desde este enlace:</p>
              <p><a href="%s">Solicitar renovación</a></p>
              <p>Para cualquier duda, contacte con
                <a href="mailto:facturacion@solunet.es">facturacion@solunet.es</a>.
              </p>
              <p>Saludos cordiales.</p>
            </body>
            </html>
            """.formatted(elementos, escaparHtml(urlRenovacion));
    }

    private String construirInformeAdminHtml(List<ErrorEnvioEmail> errores) {
        if (errores == null || errores.isEmpty()) {
            return """
                <!doctype html>
                <html lang="es">
                <body>
                  <img src="cid:logosolunet" alt="Solunet" width="180">
                  <h2>Informe de renovación de dominios</h2>
                  <p>No se han producido errores durante el proceso.</p>
                </body>
                </html>
                """;
        }

        String detalles = errores.stream()
                .map(error -> {
                    String dominios = error.dominios().stream()
                            .map(d -> "<li>" + escaparHtml(d.getNombreDominio()) + "</li>")
                            .collect(java.util.stream.Collectors.joining());

                    return "<section><h3>"
                            + escaparHtml(error.cliente().getNombre())
                            + "</h3><p>"
                            + escaparHtml(error.cliente().getEmail())
                            + "</p><ul>" + dominios + "</ul><p>"
                            + escaparHtml(error.mensajeError())
                            + "</p></section>";
                })
                .collect(java.util.stream.Collectors.joining());

        return """
            <!doctype html>
            <html lang="es">
            <body>
              <img src="cid:logosolunet" alt="Solunet" width="180">
              <h2>Informe de renovación de dominios</h2>
              <p>Se han producido errores durante el proceso:</p>
              %s
            </body>
            </html>
            """.formatted(detalles);
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}
