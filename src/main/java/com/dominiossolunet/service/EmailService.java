package com.dominiossolunet.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

@Service
public class EmailService {

    private  final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    @Value("${spring.mail.username}")
    private final String remitente;

    //Constructor

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, String remitente){
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.remitente = remitente;
    }

    // Métodos

    public boolean enviarAvisoRenovacion(){

    }

    public boolean enviarNotificacionAdmin(){}
}
