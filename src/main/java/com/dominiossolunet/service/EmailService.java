package com.dominiossolunet.service;

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

import java.util.List;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String remitente;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public boolean enviarAvisoRenovacion(Cliente cliente, List<Dominio> dominiosRenovar, String urlRenovacion) {

        boolean enviado = false;

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

            enviado = true;

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return enviado;
    }

    // Todo completar aviso
//    public boolean enviarNotificacionAdmin(){}
}
