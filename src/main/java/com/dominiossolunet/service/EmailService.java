package com.dominiossolunet.service;

import com.dominiossolunet.model.Dominio;
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

    /*public boolean enviarAvisoRenovacion(Dominio dominio){

    }

    public boolean enviarNotificacionAdmin(){}*/
}
