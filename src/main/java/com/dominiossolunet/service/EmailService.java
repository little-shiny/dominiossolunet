package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.repository.TokenRenovacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

@Service
public class EmailService {

    private  final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    @Value("${spring.mail.username}")
    private String remitente;

    //Constructor

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine){
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    // Métodos

    public boolean enviarAvisoRenovacion(Dominio dominio, String urlRenovacion){

        final String nombreCliente = dominio.getCliente().getNombre();
        final String nombreDominio = dominio.getNombreDominio();

    }

    public boolean enviarNotificacionAdmin(){}
}
