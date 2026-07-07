package com.dominiossolunet.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Getter@Setter
public class TokenRenovacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter(AccessLevel.NONE)
    private int id;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaExpiracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio")
    private Dominio dominio;

    private String token;

    private boolean usado;

}