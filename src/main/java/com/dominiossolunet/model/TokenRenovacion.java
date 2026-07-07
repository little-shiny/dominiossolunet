package com.dominiossolunet.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.users.GenericRole;

import java.time.LocalDateTime;

@Entity @Getter
public class TokenRenovacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Setter
    private LocalDateTime fechaCreacion;

    @Getter @Setter
    private LocalDateTime fechaExpiracion;

    @Getter @Setter
    private String token;

    @Getter@Setter
    private boolean usado;

}