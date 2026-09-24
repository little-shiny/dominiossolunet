package com.dominiossolunet.service;

import com.dominiossolunet.dto.ErrorEnvioEmail;
import com.dominiossolunet.dto.ResultadoEnvioEmail;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${app.email.admin}")
    private String emailAdmin;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }


//    --------------------------------------------------------------------
//    ENVÍOS DE AVISOS DE RENOVACIÓN
//    --------------------------------------------------------------------
    public ResultadoEnvioEmail enviarAvisoRenovacion(Cliente cliente, List<Dominio> dominiosRenovar, String urlRenovacion) {

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

            //Context para la plantilla de thymeleaf
            Context context = new Context();
            context.setVariable("listaDominios", dominiosRenovar);
            context.setVariable("urlRenovacion", urlRenovacion);

            String html = templateEngine.process("email/aviso-renovacion", context);

            helper.setTo(cliente.getEmail());
            helper.setFrom(remitente);
            helper.setSubject("Dominios próximos a expirar - Solunet");
            helper.setText(html, true);
            helper.addInline("logosolunet", new ClassPathResource("static/images/logosolunet.png"));
            mailSender.send(mensaje);
            logger.info("Email de renovación enviado correctamente a {}", cliente.getEmail());

            return new ResultadoEnvioEmail(true,null);

        } catch (MessagingException e) {
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

            //Context para la plantilla de thymeleaf
            Context context = new Context();
            context.setVariable("erroresEnvio", erroresEnvio);

            String html = templateEngine.process("email/informe-renovacion-admin", context);

            helper.setTo(emailAdmin);
            helper.setFrom(remitente);
            helper.setSubject("Informe de avisos de renovación dominios - " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            helper.setText(html, true);
            helper.addInline("logosolunet", new ClassPathResource("static/images/logosolunet.png"));
            mailSender.send(mensaje);
            logger.info("Email de informe enviado correctamente a {}", emailAdmin);

            return new ResultadoEnvioEmail(true,null);

        } catch (MessagingException e) {
            logger.error("Error enviando el informe de renovación al administrador",e);
            return new ResultadoEnvioEmail(false, e.getMessage());
        }
    }

}
